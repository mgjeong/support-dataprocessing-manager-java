package main

import (
    emf "go/emf"

    "fmt"
    "os"
    "os/signal"
    "log"
    "net"
)

const hostName string = "localhost"
const portNum string = "6000"
const msg_prefix string = " topic,"

var service string = hostName + ":" + portNum
var RemoteAddr  *net.UDPAddr
var conn *net.UDPConn
var err error

/*
 * Method for sending recieved message to the runtime(kapacitor)
 */
func sendEventToRuntime(msg string) {

    fmt.Printf("\nMsg to Send : %s\n", msg)

    // write a message to server
    message := []byte(msg)

    _, err = conn.Write(message)

    if err != nil {
        log.Println(err)
    } else {
        log.Println("\tEvent Delivered")
    }
}

/*
 * Callback Method for receiving message(s) thru EMF (Edge Messaging Framework)
 * Subscribed without topic
 */
func eventRecievedCB(event emf.Event) {

    var msg string = msg_prefix

    fmt.Printf("\n----------------- 1 ------------------")
    fmt.Printf("\nDevice: %s", event.GetDevice())
    fmt.Printf("\nReadings:")

    var readings []*emf.Reading = event.GetReading()
    for i := 0; i < len(readings); i++ {
        fmt.Printf("\nKey: %s", readings[i].GetName())
        fmt.Printf("\nValue: %s", readings[i].GetValue())

        msg += readings[i].GetName() + "=" + readings[i].GetValue() + " "
    }
    fmt.Printf("\n--------------------------------------\n")

    sendEventToRuntime(msg)
}

/*
 * Callback Method for receiving message(s) thru EMF (Edge Messaging Framework)
 * Subscribed with topic
 */
func eventRecievedCB2(topic string, event emf.Event) {

    var msg string = topic +", "

    fmt.Printf("\n---------------- 2 -------------------")
    fmt.Printf("\nDevice: %s", event.GetDevice())
    fmt.Printf("\nReadings:")

    var readings []*emf.Reading = event.GetReading()
    for i := 0; i < len(readings); i++ {
        fmt.Printf("\nKey: %s", readings[i].GetName())
        fmt.Printf("\nValue: %s", readings[i].GetValue())

        msg += readings[i].GetName() + "=" + readings[i].GetValue() + " "
    }
    fmt.Printf("\n--------------------------------------\n")

    sendEventToRuntime(msg)
}

/*
 * Main method
 */
func main() {

    // Read argument(s) and take it as target topic(s)
    topics := os.Args[1:]
    topicCnt := len(topics)

    // UDP related variables and intialization
    RemoteAddr, err = net.ResolveUDPAddr("udp", service)
    conn, err = net.DialUDP("udp", nil, RemoteAddr)
    if err != nil {
        log.Fatal(err)
    } else {
        log.Printf("\t[UDP_INIT] Established connection to %s \n", service)
        log.Printf("\t[UDP_INIT] UDP Server address : %s \n", conn.RemoteAddr().String())
        log.Printf("\t[UDP_INIT] UDP client address : %s \n", conn.LocalAddr().String())
    }

    // EMF Subscribe related information
    var ip string = "localhost"
    var port int = 5562
    var result emf.EMFErrorCode
    var subscriber *emf.EMFSubscriber

    //Callback registration into EMF
    subCB := func(event emf.Event) { eventRecievedCB(event) }
    subTopicCB := func(topic string, event emf.Event) {
        fmt.Printf("\nTopic: %s", topic)
        eventRecievedCB2(topic, event)
    }

    // Get EMF singleton instance
    var instance *emf.EMFAPI = emf.GetInstance()

    // Initilize the EMF SDK
    result = instance.Initialize()
    fmt.Printf("\n[EMF_INIT] Error code is: %d\n", result)

    // Subscribing Topics
    if topicCnt > 0 {
        subscriber = emf.GetEMFSubscriber(ip, port, subCB, subTopicCB)
        result = subscriber.Start()
        if result != 0 {
            fmt.Printf("Error while starting subscriber")
        }
        fmt.Printf("\n[Start] Error code is: %d\n", result)

        for i := 0; i < topicCnt ; i++ {
            //fmt.Printf("Topic[%d] : %s\n",i,topics[i])
            result = subscriber.SubscribeForTopic(topics[i])
            if result != 0 {
                fmt.Printf("Error while Subscribing : %s", topics[i])
            }
        }
    } else {
        fmt.Printf("\nInvalid choice..[Re-run application]\n")
        os.Exit(-1)
    }
    fmt.Printf("\nSuscribed to publisher.. -- Waiting for Events --\n")

    // For the graceful termination of this application with system interrupt
    signalChan := make(chan os.Signal, 1)
    cleanupDone := make(chan bool)
    signal.Notify(signalChan, os.Interrupt)
    go func() {
        for _ = range signalChan {
            fmt.Println("\nReceived an interrupt, stopping services...\n")
            //cleanup(services, c)
            cleanupDone <- true
        }
    }()

    // infinite loop for receiving messages....
    for {

        i := <-cleanupDone
        if i == true {
            instance.Terminate()
            defer conn.Close()
            break
        }
    }

    os.Exit(1)
}
