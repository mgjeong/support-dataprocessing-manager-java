package main

import (
	"os"
	"os/signal"
	"go/emf"
	"log"
	"encoding/json"
	"strconv"
	"strings"
	deliver "./deliver"
)

const msg_prefix string = " topic "
const MAX int = (5)

type Input struct {
	Type string
	Src string
	Topics [MAX]string
}

var list [MAX]Input
var inputCnt int

type Params struct {
	Input []interface{}
	Output []interface{}
}

/**
 * Callback Method for receiving message(s) thru EMF (Edge Messaging Framework)
 * From Interface Module
 * Subscribed without topic
 */
func eventReceivedFromDataSourceCB(event emf.Event) {

	var msg string = msg_prefix + " "

	log.Printf("----------------- 1 ------------------\n")
	log.Printf("Device: %s\n", event.GetDevice())
	log.Printf("Readings: \n")

	msg += "device=" +event.GetDevice()

	var readings []*emf.Reading = event.GetReading()
	for i := 0; i < len(readings); i++ {
		log.Printf("\tKey: %s\n", readings[i].GetName())
		log.Printf("\tValue: %s\n", readings[i].GetValue())

		msg += "," + readings[i].GetName() + "=" + readings[i].GetValue()

	}
	log.Printf("--------------------------------------\n")
	deliver.SendEventToRuntime(msg)
}

/**
 * Callback Method for receiving message(s) thru EMF (Edge Messaging Framework)
 * From Interface Module
 * Subscribed with topic
 */
func eventReceivedFromDataSourceCB2(topic string, event emf.Event) {

	var msg string = topic +" "

	log.Printf("---------------- 2 -------------------\n")
	log.Printf("Device: %s\n", event.GetDevice())
	log.Printf("Readings:\n")

	msg += "device=" +event.GetDevice()

	var readings []*emf.Reading = event.GetReading()
	for i := 0; i < len(readings); i++ {
		log.Printf("\tKey: %s\n", readings[i].GetName())
		log.Printf("\tValue: %s\n", readings[i].GetValue())

		msg += "," + readings[i].GetName() + "=" + readings[i].GetValue()
	}
	log.Printf("--------------------------------------\n")
	deliver.SendEventToRuntime(msg)
}

/**
 * Callback Method for receiving message(s) thru EMF (Edge Messaging Framework)
 * From Kapacitor
 * Subscribed without topic
 */
func eventReceivedFromEngineInfCB(event emf.Event) {

	log.Printf("----------------- 3 ------------------\n")
	log.Printf("Device: %s\n", event.GetDevice())
	log.Printf("Readings:\n")

	var readings []*emf.Reading = event.GetReading()
	for i := 0; i < len(readings); i++ {
		log.Printf("\tKey: %s\n", readings[i].GetName())
		log.Printf("\tValue: %s\n", readings[i].GetValue())

		if readings[i].GetName() == "params" {
			extractParameters([]byte(readings[i].GetValue()))
			subscribeData()
		}
	}
	log.Printf("\n--------------------------------------\n")
}

/**
 * Callback Method for receiving message(s) thru EMF (Edge Messaging Framework)
 * From Kapacitor
 * Subscribed with topic
 */
func eventReceivedFromEngineInfCB2(topic string, event emf.Event) {

	log.Printf("---------------- 4 -------------------\n")
	log.Printf("Device: %s\n", event.GetDevice())
	log.Printf("Readings:\n")

	var readings []*emf.Reading = event.GetReading()
	for i := 0; i < len(readings); i++ {
		log.Printf("\tKey: %s\n", readings[i].GetName())
		log.Printf("\tValue: %s\n", readings[i].GetValue())

		if readings[i].GetName() == "params" {
			extractParameters([]byte(readings[i].GetValue()))
			subscribeData()
		}
	}
	log.Printf("--------------------------------------\n")
}

/**
 * Method for extracting parameters from the Json string..
 */
func extractParameters(arg []byte) {

	// Parameter information(Json) parsing
	buf := Params{}
	if err := json.Unmarshal(arg, &buf); err != nil {
		panic(err)
	}

	inputCnt = len(buf.Input)
	log.Printf("[INGEST_DATA] Input Cnt : %d",inputCnt)

	// Loop for all the inputs
	for i := 0; i < inputCnt ; i++ {
		if i >= MAX {
			break;
		}
		item := buf.Input[i].(map[string]interface{})

		list[i].Type = item["dataType"].(string)
		list[i].Src = item["dataSource"].(string)

		top := item["topics"].([]interface{})
		topicsCnt := len(top)
		log.Printf("[INGEST_DATA] Input Type : %s, Src : %s TopicCnt : %d\n", list[i].Type, list[i].Src, topicsCnt)

		for inTopics := 0 ; inTopics < topicsCnt ; inTopics++ {
			list[i].Topics[inTopics] = top[inTopics].(string)
			log.Printf("[INGEST_DATA] Topic[%d] : %s \n", inTopics, top[inTopics])
		}
	}
}

/**
 * Method for subscribing target topics
 */
func subscribeData() {
	// TODO : Logic for managing multiple subscribers globally will be added
	// TODO : Only one subscriber is used now..
	// for testing only~!!
	//arg := []byte(`{"input":[{"dataType":"EMF","dataSource":"localhost:5562","topics":["a","b"]}],"output":[{"dataType":"UDP","dataSource":"localhost:1234"},{"dataType":"EMF","dataSource":"localhost:1234","topics":["a","b"]}]}`)
	//arg := []byte(`{"input":[],"output":[{"dataType":"UDP","dataSource":"localhost:1234"},{"dataType":"EMF","dataSource":"localhost:1234","topics":["a","b"]}]}`)
	//extractParamsters(arg)

	var result emf.EMFErrorCode
	var srcSubscriber *emf.EMFSubscriber

	// Callbacks for EMF (Connection for Data sources)
	subSrcCB := func(event emf.Event) { eventReceivedFromDataSourceCB(event) }
	subSrcTopicCB := func(topic string, event emf.Event) {
		log.Printf("[INGEST_DATA] Topic : %s\n", topic)
		eventReceivedFromDataSourceCB2(topic, event)
	}

	// Subscribing Topics to Data sources
	if inputCnt > 0 {

		str :=strings.Split(list[0].Src, ":")
		ip := str[0]
		port, err := strconv.Atoi(str[1])
		if err != nil {
			log.Println(err)
		} else {
			srcSubscriber = emf.GetEMFSubscriber(ip, port, subSrcCB, subSrcTopicCB)
			result = srcSubscriber.Start()
			if result != 0 {
				log.Fatal("\t[INGEST_DATA] Error while starting subscriber\n")
			}
			log.Printf("\t[INGEST_DATA] Error code is: %d\n", result)

			topicCnt := len(list[0].Topics)
			for i := 0; i < topicCnt ; i++ {
				if list[0].Topics[i] != "" {
					result = srcSubscriber.SubscribeForTopic(list[0].Topics[i])
					if result != 0 {
						log.Printf("\t[INGEST_DATA] Error while Subscribing : %s\n", list[0].Topics[i])
					}
				}
			}
		}
		log.Println("[INGEST_DATA] Suscribed to publisher.. -- Waiting for Events --")
	} else {
		//log.Printf("[INGEST_DATA] Invalid choice..[Re-run application]\n")
		//os.Exit(-1)
		log.Println("[INGEST_DATA] Nothing subscribe .... ")
	}
}

/**
 * Main method
 */
func main() {

	var instance *emf.EMFAPI

	// Read argument(s)
	kapaHost := os.Args[1]
	kapaPort := os.Args[2]
	infHost := os.Args[3]
	infPort := os.Args[4]

	log.Printf("[INGEST_DATA] Kapacitor IP : %s, Port %s\n", kapaHost, kapaPort)
	log.Printf("[INGEST_DATA] Interface IP : %s, Port %s\n", infHost, infPort)

	// Kapacitor(UDP) connection initialization
	var res = deliver.EstablishConnection(kapaHost, kapaPort)
	if res != false {

		// EMF Subscribe related information
		var result emf.EMFErrorCode
		var infSubscriber *emf.EMFSubscriber

		// Get EMF singleton instance
		instance = emf.GetInstance()

		// Initialize the EMF library
		result = instance.Initialize()
		log.Printf("[INGEST_DATA] EMF Init err : %d\n", result)


		// Callbacks for EMF (Connection for Interface Module)
		subInfCB := func(event emf.Event) { eventReceivedFromEngineInfCB(event) }
		subInfTopicCB := func(topic string, event emf.Event) {
			log.Printf("[INGEST_DATA] Topic : %s\n", topic)
			eventReceivedFromEngineInfCB2(topic, event)
		}
		infPortInt, _ := strconv.Atoi(infPort)
		infSubscriber = emf.GetEMFSubscriber(infHost, infPortInt, subInfCB, subInfTopicCB)
		result = infSubscriber.Start()
		result = infSubscriber.Subscribe()
		if result != 0 {
			log.Fatal("[INGEST_DATA] Error while starting subscriber to Interface \n")
		}
		log.Printf("[INGEST_DATA] Error code is: %d\n", result)
	}

	// For the graceful termination of this application with system interrupt
	signalChan := make(chan os.Signal, 1)
	cleanupDone := make(chan bool)
	signal.Notify(signalChan, os.Interrupt)
	go func() {
		for _ = range signalChan {
			log.Println("[INGEST_DATA] Received an interrupt, stopping services...\n")
			cleanupDone <- true
		}
	}()

	// infinite loop for receiving messages....
	for {
		i := <-cleanupDone
		if i == true {
			instance.Terminate()
			deliver.TerminateConnection()
			break
		}
	}

	os.Exit(1)
}

