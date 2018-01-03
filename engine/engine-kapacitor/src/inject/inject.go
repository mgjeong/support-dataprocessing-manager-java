package main

import (
	"github.com/influxdata/kapacitor/udf/agent"
	"log"
	"os"
	"inject/injectHandler"
)

func main() {
	thisAgent := agent.New(os.Stdin, os.Stdout)
	thisHandler := injectHandler.NewInjectHandler(thisAgent)
	thisAgent.Handler = thisHandler

	log.Println("Starting injecting agent: PID", os.Getpid())
	thisAgent.Start()
	err := thisAgent.Wait()
	if err != nil {
		log.Fatal(err)
	}
	log.Println("Finishing injecting agent: PID", os.Getpid())
}
