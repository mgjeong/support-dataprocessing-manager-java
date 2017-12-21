package sink

import (
	"testing"
	"io/ioutil"
	"os"
	"fmt"
)

const testFilename = "./testfilesink.txt"
const key = "id"
const value = "testdata"
var expectedResult = fmt.Sprintf("{\"%s\":\"%s\"}\n", key, value)

func TestFileSink_AddSink(t *testing.T) {
	testSink := new(FileSink)
	err := testSink.AddSink(testFilename, "")
	if err != nil {
		t.Error(err)
	}
	testMap := make(map[string]interface{})
	testMap[key] = value
	testSink.Flush(&testMap)
	if err != nil {
		t.Error(err)
	}
	testSink.Close()

	var data []byte
	data, err = ioutil.ReadFile(testFilename)
	if err != nil {
		t.Error(err)
	}
	if string(data) != expectedResult {
		t.Log("Written context:", string(data), ", but expected:", expectedResult)
		t.Error("Wrong context was written")
	}
	err = os.Remove(testFilename)
	if err != nil {
		t.Error("cannot remove testfile")
	}
}
