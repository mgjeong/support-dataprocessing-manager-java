package sink

import (
	"log"
	"github.com/mgjeong/messaging-zmq/go/emf"
	"strings"
	"strconv"
	"errors"
	"encoding/json"
	"time"
	"os"
)

type MQSink struct {
	targetEMF *emf.EMFPublisher
	topic     string
}

func (m *MQSink) AddSink(address, topic string) error {
	log.Println("Initializing emf sink for", address)
	target := strings.Split(address, ":")
	emf.GetInstance().Initialize()
	startCB := func(code emf.EMFErrorCode) { log.Println("EMF starting by callback") }
	stopCB := func(code emf.EMFErrorCode) { log.Println("EMF stopping by callback") }
	errorCB := func(code emf.EMFErrorCode) { log.Println("EMF error by callback") }
	port, err := strconv.Atoi(target[1])
	if err != nil {
		return errors.New("error: wrong port number for emf")
	}
	m.targetEMF = emf.GetEMFPublisher(port, startCB, stopCB, errorCB)
	m.topic = topic
	result := m.targetEMF.Start()

	if result != 0 {
		return errors.New("error: failed to start emf publisher")
	}
	return nil
}

func (m *MQSink) Flush(record *map[string]interface{}) error {
	jsonBytes, err := json.Marshal(record)
	log.Println("Writing: ", string(jsonBytes))
	var event = getEvent(jsonBytes)
	var result emf.EMFErrorCode
	if m.topic == "" {
		result = m.targetEMF.Publish(event)
	} else {
		result = m.targetEMF.PublishOnTopic(m.topic, event)
	}
	if result != 0 {
		err = errors.New("failed to publish the record")
	}

	return err
}

func getEvent(data []byte) emf.Event {
	var event emf.Event

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

	var reading = &emf.Reading{}
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

	event.Reading = make([]*emf.Reading, 1)
	event.Reading[0] = reading
	return event
}

func (m *MQSink) Close() {
	if m.targetEMF != nil {
		m.targetEMF.Stop()
	}
}
