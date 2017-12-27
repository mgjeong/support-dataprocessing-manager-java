package sink

import (
	"log"
	"strings"
	"strconv"
	"errors"
	"encoding/json"
	"time"
	"os"
	"github.com/mgjeong/protocol-ezmq-go/ezmq"
)

type EZMQSink struct {
	targetEZMQ *ezmq.EZMQPublisher
	topic      string
}

func (m *EZMQSink) AddSink(address, topic string) error {
	log.Println("Initializing ezmq sink for", address)
	target := strings.Split(address, ":")
	ezmq.GetInstance().Initialize()
	startCB := func(code ezmq.EZMQErrorCode) { log.Println("EZMQ starting by callback") }
	stopCB := func(code ezmq.EZMQErrorCode) { log.Println("EZMQ stopping by callback") }
	errorCB := func(code ezmq.EZMQErrorCode) { log.Println("EZMQ error by callback") }
	port, err := strconv.Atoi(target[1])
	if err != nil {
		return errors.New("error: wrong port number for ezmq")
	}
	m.targetEZMQ = ezmq.GetEZMQPublisher(port, startCB, stopCB, errorCB)
	m.topic = topic
	result := m.targetEZMQ.Start()

	if result != 0 {
		return errors.New("error: failed to start ezmq publisher")
	}
	return nil
}

func (m *EZMQSink) Flush(record *map[string]interface{}) error {
	jsonBytes, err := json.Marshal(record)
	log.Println("Writing: ", string(jsonBytes))
	var event = getEvent(jsonBytes)
	var result ezmq.EZMQErrorCode
	if m.topic == "" {
		result = m.targetEZMQ.Publish(event)
	} else {
		result = m.targetEZMQ.PublishOnTopic(m.topic, event)
	}
	if result != 0 {
		err = errors.New("failed to publish the record")
	}

	return err
}

func getEvent(data []byte) ezmq.Event {
	var event ezmq.Event

	var id string = "DPR-kapacitor"
	var now int64 = time.Now().UnixNano()
	var created int64 = 0
	var modified int64 = 0
	var origin int64 = now
	var pushed int64 = now
	device, _ := os.Hostname()

	event.Id = &id
	event.Created = &created
	event.Modified = &modified
	event.Origin = &origin
	event.Pushed = &pushed
	event.Device = &device

	var reading = &ezmq.Reading{}
	var rId string = "DPR-kapacitor"
	var rCreated int64 = 0
	var rModified int64 = 0
	var rOrigin int64 = now
	var rPushed int64 = now
	rDevice, _ := os.Hostname()
	var rName string = "DPR"
	var rValue = string(data)
	reading.Id = &rId
	reading.Created = &rCreated
	reading.Modified = &rModified
	reading.Origin = &rOrigin
	reading.Pushed = &rPushed
	reading.Device = &rDevice
	reading.Name = &rName
	reading.Value = &rValue

	event.Reading = make([]*ezmq.Reading, 1)
	event.Reading[0] = reading
	return event
}

func (m *EZMQSink) Close() {
	if m.targetEZMQ != nil {
		m.targetEZMQ.Stop()
	}
}
