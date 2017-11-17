package main

import (
	emf "go/emf"

	"fmt"
	"os"
	"time"
)

func getEvent() emf.Event {
	var event emf.Event

	var id string = "id1"
	event.Id = &id
	var created int64 = 1
	event.Created = &created
	var modified int64 = 2
	event.Modified = &modified
	var origin int64 = 3
	event.Origin = &origin
	var pushed int64 = 4
	event.Pushed = &pushed
	var device string = "device1"
	event.Device = &device

	//form the reading
	var reading1 *emf.Reading = &emf.Reading{}
	var rId string = "id1"
	reading1.Id = &rId
	var rCreated int64 = 1
	reading1.Created = &rCreated
	var rModified int64 = 2
	reading1.Modified = &rModified
	var rOrigin int64 = 3
	reading1.Origin = &rOrigin
	var rPushed int64 = 4
	reading1.Pushed = &rPushed
	var rDevice string = "device1"
	reading1.Device = &rDevice
	var rName string = "temperature"
	reading1.Name = &rName
	var rValue string = "20"
	reading1.Value = &rValue

	event.Reading = make([]*emf.Reading, 1)
	event.Reading[0] = reading1
	return event
}

func main() {
	var port int = 5563
	var result emf.EMFErrorCode
	var publisher *emf.EMFPublisher
	startCB := func(code emf.EMFErrorCode) { fmt.Printf("startCB") }
	stopCB := func(code emf.EMFErrorCode) { fmt.Printf("stopCB") }
	errorCB := func(code emf.EMFErrorCode) { fmt.Printf("errorCB") }

	//get singleton instance
	var instance *emf.EMFAPI = emf.GetInstance()

	//Initilize the EMF SDK
	result = instance.Initialize()
	fmt.Printf("\n[Initialize] Error code is: %d", result)

	//User choice
	var choice int
	var topic string
	fmt.Printf("\nEnter 1 for General Event testing")
	fmt.Printf("\nEnter 2 for Topic Based delivery\n")
	fmt.Scanf("%d", &choice)

	switch choice {
	case 1:
		publisher = emf.GetEMFPublisher(port, startCB, stopCB, errorCB)
	case 2:
		publisher = emf.GetEMFPublisher(port, startCB, stopCB, errorCB)
		fmt.Printf("\nEnter the topic: ")
		fmt.Scanf("%s", &topic)
		fmt.Printf("Topic is: %s\n", topic)
	default:
		fmt.Printf("\nInvalid choice..[Re-run application]\n")
		os.Exit(-1)
	}

	//start publisher
	result = publisher.Start()
	if result != 0 {
		fmt.Printf("\nError while starting publisher\n")
	}
	fmt.Printf("\n[Start] Error code is: %d", result)

	var event emf.Event = getEvent()
	fmt.Printf("\n--------- Will Publish 15 events at interval of 1 seconds ---------\n")
	for i := 0; i < 15 ; i++ {
		if topic == "" {
			result = publisher.Publish(event)
		} else {
			result = publisher.PublishOnTopic(topic, event)
		}
		if result != 0 {
			fmt.Printf("\nError while publishing")
		}
		fmt.Printf("\nPublished event result: %d\n", result)
		time.Sleep(1000 * time.Millisecond)
	}

	//stop publisher
	result = publisher.Stop()
	if result != 0 {
		fmt.Printf("Error while Stopping publisher")
	}
	fmt.Printf("\n[Stop] Error code is: %d\n", result)
}
