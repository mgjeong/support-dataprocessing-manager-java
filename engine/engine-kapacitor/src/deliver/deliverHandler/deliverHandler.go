package deliverHandler

import (
	"errors"
	"log"
	"github.com/influxdata/kapacitor/udf/agent"
	"deliver/deliverHandler/sink"
)

type deliverHandler struct {
	sinkType string
	address  string
	topic    string
	sink     sink.StreamSink
	agent    *agent.Agent
}

func NewDeliverHandler(agent *agent.Agent) *deliverHandler {
	return &deliverHandler{agent: agent}
}

// Return the InfoResponse. Describing the properties of this Handler
func (d *deliverHandler) Info() (*agent.InfoResponse, error) {
	info := &agent.InfoResponse{
		Wants:    agent.EdgeType_STREAM,
		Provides: agent.EdgeType_STREAM,
		Options: map[string]*agent.OptionInfo{
			"sink":    {ValueTypes: []agent.ValueType{agent.ValueType_STRING}},
			"address": {ValueTypes: []agent.ValueType{agent.ValueType_STRING}},
			"topic":   {ValueTypes: []agent.ValueType{agent.ValueType_STRING}},
		},
	}
	return info, nil
}

// Initialize the Handler with the provided options.
func (d *deliverHandler) Init(r *agent.InitRequest) (*agent.InitResponse, error) {
	init := &agent.InitResponse{
		Success: true,
		Error:   "",
	}

	for _, opt := range r.Options {
		switch opt.Name {
		case "sink":
			d.sinkType = opt.Values[0].Value.(*agent.OptionValue_StringValue).StringValue
		case "address":
			d.address = opt.Values[0].Value.(*agent.OptionValue_StringValue).StringValue
		case "topic":
			d.topic = opt.Values[0].Value.(*agent.OptionValue_StringValue).StringValue
		}
	}

	switch d.sinkType {
	case "emf":
		d.sink = new(sink.MQSink)
	case "mongodb":
		d.sink = new(sink.MongoDBSink)
	case "f":
		d.sink = new(sink.FileSink)
	default:
		return nil, errors.New("sink must be specified")
	}

	err := d.sink.AddSink(d.address, d.topic)

	return init, err
}

// Create a snapshot of the running state of the handler.
func (d *deliverHandler) Snapshot() (*agent.SnapshotResponse, error) {
	return &agent.SnapshotResponse{}, nil
}

// Restore a previous snapshot.
func (d *deliverHandler) Restore(req *agent.RestoreRequest) (*agent.RestoreResponse, error) {
	if d.sink.AddSink(d.address, d.topic) != nil {
		return &agent.RestoreResponse{
			Success: false,
		}, errors.New("error: fail to restore sink")
	}
	return &agent.RestoreResponse{
		Success: true,
	}, nil
}

// A batch has begun.
func (d *deliverHandler) BeginBatch(*agent.BeginBatch) error {
	return errors.New("batching not supported")
}

// A point has arrived.
func (d *deliverHandler) Point(p *agent.Point) error {
	output := make(map[string]interface{})
	for key, value := range p.FieldsBool {
		output[key] = value
	}

	for key, value := range p.FieldsDouble {
		output[key] = value
	}

	for key, value := range p.FieldsInt {
		output[key] = value
	}

	for key, value := range p.FieldsString {
		output[key] = value
	}

	for key, value := range p.Tags {
		output[key] = value
	}

	err := d.sink.Flush(&output)

	d.agent.Responses <- &agent.Response{
		Message: &agent.Response_Point{
			Point: p,
		},
	}
	return err
}

// The batch is complete.
func (d *deliverHandler) EndBatch(*agent.EndBatch) error {
	return errors.New("batching not supported")
}

// Gracefully stop the Handler.
// No other methods will be called.
func (d *deliverHandler) Stop() {
	log.Println("Closing sink")
	if d.sink != nil {
		d.sink.Close()
	}
	close(d.agent.Responses)
}
