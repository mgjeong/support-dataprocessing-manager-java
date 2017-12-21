package sink

import (
	"os"
	"log"
	"encoding/json"
)

type FileSink struct {
	targetFile *os.File
}

func (f *FileSink) AddSink(address, topic string) error {
	log.Println("Add a file sink", address)
	destination, err := os.Create(address)
	f.targetFile = destination
	return err
}

func (f *FileSink) Flush(record *map[string]interface{}) error {
	jsonBytes, err := json.Marshal(record)
	log.Println("Writing: ", string(jsonBytes))
	f.targetFile.Write(jsonBytes)
	f.targetFile.WriteString("\n")
	return err
}

func (f *FileSink) Close() {
	if f.targetFile != nil {
		f.targetFile.Close()
	}
}
