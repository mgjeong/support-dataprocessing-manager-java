package injectHandler

import (
	"log"
	"errors"
	"context"
	"net"
	"os"
	"strings"
	"strconv"
	"fmt"
	"github.com/influxdata/kapacitor/udf/agent"
	"github.com/mgjeong/messaging-zmq/go/emf"
	"encoding/json"
	"time"
	"sync"
)

type injectHandler struct {
	source  string
	address string
	topic   string

	childContext context.Context
	cancel       context.CancelFunc

	agent *agent.Agent
}

var conn *net.UDPConn
var table string

func NewInjectHandler(agent *agent.Agent) *injectHandler {
	return &injectHandler{agent: agent}
}

func (p *injectHandler) Info() (*agent.InfoResponse, error) {
	info := &agent.InfoResponse{
		Wants:    agent.EdgeType_STREAM,
		Provides: agent.EdgeType_STREAM,
		Options: map[string]*agent.OptionInfo{
			"source":  {ValueTypes: []agent.ValueType{agent.ValueType_STRING}},
			"address": {ValueTypes: []agent.ValueType{agent.ValueType_STRING}},
			"topic":   {ValueTypes: []agent.ValueType{agent.ValueType_STRING}},
			"into":    {ValueTypes: []agent.ValueType{agent.ValueType_STRING}},
		},
	}
	return info, nil
}

func (p *injectHandler) Init(r *agent.InitRequest) (*agent.InitResponse, error) {
	init := &agent.InitResponse{
		Success: true,
		Error:   "",
	}

	for _, opt := range r.Options {
		switch opt.Name {
		case "source":
			p.source = opt.Values[0].Value.(*agent.OptionValue_StringValue).StringValue
		case "address":
			p.address = opt.Values[0].Value.(*agent.OptionValue_StringValue).StringValue
		case "topic":
			p.topic = opt.Values[0].Value.(*agent.OptionValue_StringValue).StringValue
		case "into":
			table = opt.Values[0].Value.(*agent.OptionValue_StringValue).StringValue
		}
	}

	if p.address == "" {
		return nil, errors.New("address must be specified")
	}

	if p.source == "" {
		return nil, errors.New("source must be specified")
	}

	if table == "" {
		return nil, errors.New("target table must be specified")
	}

	log.Println("Waiting to make source in PID ", os.Getpid())
	p.childContext, p.cancel = context.WithCancel(context.Background())

	var initError error = nil
	var waitingInit sync.WaitGroup
	waitingInit.Add(1)

	go func(ctx context.Context) {
		// TODO: Read UDP port dynamically
		defer waitingInit.Done()
		const udpPort = "9100"
		var emfInstance *emf.EMFAPI = nil
		var emfSub *emf.EMFSubscriber
		var udpAddr *net.UDPAddr
		udpAddr, initError = net.ResolveUDPAddr("udp", "localhost:"+udpPort)
		if initError != nil {
			return
		}
		conn, initError = net.DialUDP("udp", nil, udpAddr)
		if initError != nil {
			return
		}
		defer conn.Close()

		emfInstance = initializeEMF()
		emfSub, initError = addSource(p.topic, p.address)
		if initError != nil {
			return
		}
		defer emfSub.Stop()

		log.Println("Ready to transfer message into kapacitor")
		waitingInit.Done()

		for {
			select {
			case <-ctx.Done():
				if conn != nil {
					log.Println("Closing UDP connection")
					conn.Close()
				}
				if emfInstance != nil {
					log.Println("Terminating EMF")
					emfSub.Stop()
				}
				return
			default:
				time.Sleep(100 * time.Microsecond)
			}
		}
	}(p.childContext)

	if timedWait(&waitingInit, time.Minute) {
		return nil, errors.New("failed to initialize")
	}

	log.Println("Ready to inject from", p.address)
	return init, initError
}

func initializeEMF() *emf.EMFAPI {
	instance := emf.GetInstance()
	result := instance.Initialize()
	log.Println("Initializing EMF, error code: ", result)
	return instance
}

func addSource(topic string, hostname string) (*emf.EMFSubscriber, error) {
	log.Println("Start to make source [", hostname, "] in PID ", os.Getpid())
	target := strings.Split(hostname, ":")
	port, err := strconv.Atoi(target[1])
	if err != nil {
		return nil, errors.New("invalid port number")
	}

	subCB := func(event emf.Event) { eventHandler(event) }
	subTopicCB := func(topic string, event emf.Event) { eventHandler(event) }

	subscriber := emf.GetEMFSubscriber(target[0], port, subCB, subTopicCB)
	result := subscriber.Start()
	if result != emf.EMF_OK {
		return nil, errors.New("failed to subscription")
	}

	if topic != "" {
		result = subscriber.SubscribeForTopic(topic)
	} else {
		result = subscriber.Subscribe()
	}

	log.Println("subscriber is working with error ", result)
	return subscriber, nil
}

func eventHandler(event emf.Event) {
	var msg string

	msg = table + " "
	readings := event.GetReading()
	timeStamp := ""
	for i := 0; i < len(readings); i++ {
		body, timeStamped := jsonIntoInfluxBody(readings[i].GetValue())
		msg += body
		if timeStamped != "" {
			timeStamp = timeStamped
		}
	}
	msg = msg[:len(msg)-1]

	if timeStamp != "" {
		msg += " " + timeStamp
	}

	log.Println("message: ", msg)

	forwardEventToKapacitor(msg)
}

func jsonIntoInfluxBody(msg string) (string, string) {
	var body string
	data := make(map[string]interface{})
	decoder := json.NewDecoder(strings.NewReader(msg))
	decoder.UseNumber()
	decoder.Decode(&data)
	var timeStamp = ""
	for key, value := range data {
		var stringValue string
		switch value.(type) {
		case string:
			stringValue = value.(string)
			body += key + "=" + fmt.Sprintf("\"%s\",", value.(string))
		case json.Number:
			stringValue = value.(json.Number).String()
			body += key + "=" + stringValue + ","
		}

		// Custom conditional statements for timestamp
		if key == "sTime" || key == "timestamp" {
			timeStamp = stringValue
		}
	}
	return body, timeStamp
}

func forwardEventToKapacitor(msg string) {
	_, err := conn.Write([]byte(msg))
	if err != nil {
		errors.New("failed to forward msg via UDP")
	}
}

func (p *injectHandler) Snapshot() (*agent.SnapshotResponse, error) {
	return &agent.SnapshotResponse{}, nil
}

func (p *injectHandler) Restore(req *agent.RestoreRequest) (*agent.RestoreResponse, error) {
	// Currently, all the information necessary is set when Init() is called
	// Therefore, bypass this function
	return &agent.RestoreResponse{
		Success: true,
	}, nil
}

func (p *injectHandler) BeginBatch(batch *agent.BeginBatch) error {
	return errors.New("batching is not supported")
}

func (p *injectHandler) Point(point *agent.Point) error {
	p.agent.Responses <- &agent.Response{
		Message: &agent.Response_Point{
			Point: point,
		},
	}
	return nil
}

func (p *injectHandler) EndBatch(batch *agent.EndBatch) error {
	return nil
}

func (p *injectHandler) Stop() {
	log.Println("Stopping UDF")
	if p.childContext != nil {
		p.cancel()
	}
	close(p.agent.Responses)
}
