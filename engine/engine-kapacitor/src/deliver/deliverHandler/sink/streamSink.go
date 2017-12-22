package sink

type StreamSink interface {
	AddSink(address, topic string) error
	Flush(record *map[string]interface{}) error
	Close()
}
