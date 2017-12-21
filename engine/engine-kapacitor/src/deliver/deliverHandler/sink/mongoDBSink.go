package sink

import (
	"log"
	"gopkg.in/mgo.v2"
	"time"
	"errors"
	"strings"
)

type MongoDBSink struct {
	session    *mgo.Session
	collection *mgo.Collection
}

func (m *MongoDBSink) AddSink(address, topic string) error {
	log.Println("DPRuntime mongoDB sink")
	var err error
	// Kapacitor starts to receive data regardless of mongoDB connected
	// If session fails, this Kapacitor task will be stopped
	// This should be fixed not to start and reported to Runtime in the first place
	m.session, err = mgo.DialWithTimeout(address, 5*time.Second)
	if err != nil {
		return errors.New("error: failed to connect MongoDB")
	}

	if topic == "" {
		return errors.New("error: DB and collection names must be provided")
	}

	dbSplits := strings.Split(topic, ":")
	if len(dbSplits) != 2 {
		return errors.New("error: DB and collection must be specified as DB:COLLECTION")
	}
	m.collection = m.session.DB(dbSplits[0]).C(dbSplits[1])
	return nil
}

func (m *MongoDBSink) Flush(record *map[string]interface{}) error {
	log.Println("Writing into MongoDB")
	err := m.collection.Insert(record)
	return err
}

func (m *MongoDBSink) Close() {
	if m.session != nil {
		m.session.Close()
	}
}
