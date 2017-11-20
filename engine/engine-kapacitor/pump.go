package main

import (
	"github.com/influxdata/kapacitor/udf/agent"
	"log"
	"os"
	"context"
	"errors"
	"github.com/mgjeong/messaging-zmq/go/emf"
	"strconv"
	"strings"
	"github.com/influxdata/kapacitor/services/udp"
	"net"
)

type pumpHandler struct {
	address string
	table string

	childContext context.Context
	cancel       context.CancelFunc

	agent *agent.Agent
}

func newPumpHandler(agent *agent.Agent) *pumpHandler {
	return &pumpHandler{agent: agent}
}

func (p *pumpHandler) Info() (*agent.InfoResponse, error) {
	info := &agent.InfoResponse{
		Wants:    agent.EdgeType_STREAM,
		Provides: agent.EdgeType_STREAM,
		Options: map[string]*agent.OptionInfo{
			"source":  {ValueTypes: []agent.ValueType{agent.ValueType_STRING}},
			"address": {ValueTypes: []agent.ValueType{agent.ValueType_STRING}},
		},
	}
	return info, nil
}

func (p *pumpHandler) Init(r *agent.InitRequest) (*agent.InitResponse, error) {
	init := &agent.InitResponse{
		Success: true,
		Error:   "",
	}

	for _, opt := range r.Options {
		switch opt.Name {
		case "address":
			p.address = opt.Values[0].Value.(*agent.OptionValue_StringValue).StringValue
		case "table":
			p.table = opt.Values[0].Value.(*agent.OptionValue_StringValue).StringValue
		}
	}

	if p.address == "" {
		init.Success = false
		init.Error += " must supply source address"
	}

	if p.table == "" {
		init.Success = false
		init.Error += " must specify table name"
	}

	log.Println("KRKIM Wait to make source in PID ", os.Getpid())
	p.childContext, p.cancel = context.WithCancel(context.Background())

	go func(ctx context.Context) {
		// TODO: Read UDP port dynamically
		const udpPort = "9100"
		var emfInstance *emf.EMFAPI = nil
		var emfSub *emf.EMFSubscriber
		var conn net.Conn
		init := true
		for {
			select {
			case <-ctx.Done():
				if conn != nil {
					conn.Close()
				}
				if emfInstance != nil {
					log.Println("Terminating EMF")
					emfSub.Stop()
				}
				return
			default:
				if init {
					connection, error := net.Dial("udp", "localhost:" + udpPort)
					conn = connection
					if error != nil {
						// TODO: error handling
						log.Println("KRKIM: fail to make udp connection")
					}
					emfInstance = initializeEMF()
					emfSub = addSource(p.address)
					init = false
				}
			}
		}
	}(p.childContext)

	// TODO: Handle errors during setting EMF subscriber
	return init, nil
}

func initializeEMF() *emf.EMFAPI {
	instance := emf.GetInstance()
	result := instance.Initialize()
	log.Println("Initializing EMF, error code: ", result)
	return instance
}

func addSource(hostname string) *emf.EMFSubscriber {
	log.Println("KRKIM Start to make source [", hostname, "] in PID ", os.Getpid())
	target := strings.Split(hostname, ":")
	port, err := strconv.Atoi(target[1])
	subCB := func(event emf.Event) { eventHandler(event) }
	subTopicCB := func(topic string, event emf.Event) { eventHandler(event) }
	if err != nil {
		// TODO: error handling
		log.Println("KRKIM wrong port number")
	}

	subscriber := emf.GetEMFSubscriber(target[0], port, subCB, subTopicCB)
	result := subscriber.Start()
	// TODO: error handling
	log.Println("KRKIM subscriber started with error ", result)

	result = subscriber.Subscribe()
	// TODO: error handling
	log.Println("KRKIM subscriber is working with error ", result)
	return subscriber
}

func eventHandler(event emf.Event) {
	readings := event.GetReading()
	for i := 0; i < len(readings); i++ {
		// TODO: Assemble keys and values into influx line, and send via UDP
		log.Println("KRKIM message: ", readings[i].GetValue())
	}
}

func (p *pumpHandler) Snapshot() (*agent.SnapshotResponse, error) {
	return &agent.SnapshotResponse{}, nil
}

func (p *pumpHandler) Restore(req *agent.RestoreRequest) (*agent.RestoreResponse, error) {
	// TODO: implement
	return &agent.RestoreResponse{
		Success: true,
	}, nil
}

func (p *pumpHandler) BeginBatch(batch *agent.BeginBatch) error {
	return errors.New("batching is not supported")
}

var count int64 = 0

func (p *pumpHandler) Point(point *agent.Point) error {
	p.agent.Responses <- &agent.Response{
		Message: &agent.Response_Point{
			Point: point,
		},
	}
	log.Println("Pointing ", count)
	count = count + 1
	return nil
}

func (p *pumpHandler) EndBatch(batch *agent.EndBatch) error {
	return nil
}

func (p *pumpHandler) Stop() {
	log.Println("KRKIM Stopping UDF")
	p.cancel()
	close(p.agent.Responses)
}

func main() {
	thisAgent := agent.New(os.Stdin, os.Stdout)
	thisHandler := newPumpHandler(thisAgent)
	thisAgent.Handler = thisHandler

	log.Println("Starting agent")
	thisAgent.Start()
	err := thisAgent.Wait()
	if err != nil {
		log.Fatal(err)
	}
}
