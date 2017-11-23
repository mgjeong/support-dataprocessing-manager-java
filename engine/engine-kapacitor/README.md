Engine for Kapacitor
================================

Data Ingest-Delivery and Kapacitor interfacing module.
Kapacitor is an open-source which supports stream processing & data query
- Reference (https://github.com/influxdata/kapacitor)

### Prerequisites ###

- Install Go Language
  - Reference (https://golang.org/doc/install)

- IDE
  - GoLand (recommended, https://www.jetbrains.com/go/)
  - Or any IDE which supports Go language

### Build source ###

- Go to the location where the codebase is download & build the code

  `- /home/{home}$ cd {location of codebase}`

  `- /home/{home}/{location of given source}$ go build ./ingest_message.go`

### Running Engine ###

- Execute the built binary
  - 1st Argument : Ip address of the Kapacitor
  - 2nd Argument : Port number of the Kapacitor
  - 3rd Argument : Ip address of the Interface (which manages this module, internal usage)
  - 4th Argument : Port number of the Interface (which manages this module, internal usage)

    `- /home/{home}/{location of given source}$./ingest_message localhost 1234`

### Test Engine ###

TBD
