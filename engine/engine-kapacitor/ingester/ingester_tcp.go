package ingester

import (
	"net"
	"fmt"
	"log"
	"sync"
)

var listner net.Listener

/**
 * Nethod for intializing the TCP Connecton (Worker)
 */
func InitTCPConnection(host string, port string) bool {

	var wg sync.WaitGroup
	wg.Add(1)
	go EstablishConnection(host, port, &wg)
	wg.Wait()

	return true
}

/**
 * Method for establishing TCP connection
 */
func EstablishConnection(host string, port string, wg *sync.WaitGroup) bool {

	// Listen for incoming connections.
	listner, err := net.Listen("tcp", host+":"+port)
	if err != nil {
		log.Println("[TCP_INIT] Error listening:", err.Error())
	}
	// Close the listener when the application closes.
	defer listner.Close()
	log.Println("[TCP_INIT] Listening on " + host + ":" + port)
	for {
		// Listen for an incoming connection.
		conn, err := listner.Accept()
		if err != nil {
			log.Println("[TCP_INIT] Error accepting: ", err.Error())
		}
		// Handle connections in a new goroutine.
		go HandleRequest(conn)
	}
}

/**
 * Handles incoming requests.
 */
func HandleRequest(conn net.Conn) {
	// Make a buffer to hold incoming data.
	buf := make([]byte, 1024)
	// Read the incoming connection into the buffer.
	reqLen, err := conn.Read(buf)
	if err != nil {
		fmt.Println("[TCP_RECEIVE]Error reading:", err.Error())
	}
	log.Printf("[TCP_RECEIVE] DATA(len : %d) : %b",reqLen, buf)
	// Send a response back to person contacting us.
	// conn.Write([]byte("Message received."))
	// Close the connection when you're done with it.
	// conn.Close()
}

/**
 * Method for closing UDP connection
 */
func TerminateConnection() {
	if listner != nil {
		listner.Close()
		log.Println("\t[UDP_TERM] UDP connection closed")
	} else {
		log.Println("\t[UDP_TERM] UDP connection is already closed")
	}
}
