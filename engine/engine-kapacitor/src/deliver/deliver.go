package main

import (
	"github.com/influxdata/kapacitor/udf/agent"
	"log"
	"os"
	"deliver/deliverHandler"
)

func main() {
	thisAgent := agent.New(os.Stdin, os.Stdout)
	thisHandler := deliverHandler.NewDeliverHandler(thisAgent)
	thisAgent.Handler = thisHandler

	log.Println("Starting delivering agent: PID", os.Getpid())
	thisAgent.Start()
	err := thisAgent.Wait()
	if err != nil {
		log.Fatal(err)
	}
	log.Println("Stopping delivering agent: PID", os.Getpid())
}
