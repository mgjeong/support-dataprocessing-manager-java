Engine Kapacitor
===============

Kapacitor can be connected to Data Processing Runtime as a data query engine.
User-defined functions (UDFs) are required for utilizing kapacitor with data processing runtime.

### 1. Deploying locally ###

```
$ cd engine/engine-kapacitor
$ GOPATH=$GOPATH:$(pwd) go build src/inject/inject.go
$ GOPATH=$GOPATH:$(pwd) go build src/deliver/deliver.go
$ cp inject {CUSTOM_PATH}
$ cp deliver {CUSTOM_PATH}
```
and add following configurations for Kapacitor
```
[[udp]]
  enabled = true
  bind-address = ":9100"
  database = "dpruntime"
  retention-policy = "autogen"
[udf]
[udf.functions]
    [udf.functions.inject]
        prog = "{CUSTOM_PATH}/inject"
        timeout = "10s"
    [udf.functions.deliver]
        prog = "{CUSTOM_PATH}/deliver"
        timeout = "10s"
```
<br>

### 2. Deploying as a Docker container ###
Scripts are provided to build docker image containing Kapacitor with UDFs 
- x64
```
$ cd engine/engine-kapacitor
$ ./build.sh
$ sudo docker build -t kapacitor -f ./Dockerfile .
$ sudo docker run -it -p 9092:9092 kapacitor
```

- ARM
```
$ cd engine/engine-kapacitor
$ ./build_arm.sh
$ sudo docker build -t kapacitor -f ./Dockerfile_arm .
$ sudo docker run -it -p 9092:9092 kapacitor
```
- ARM64
```
$ cd engine/engine-kapacitor
$ ./build_arm64.sh
$ sudo docker build -t kapacitor -f ./Dockerfile_arm64 .
$ sudo docker run -it -p 9092:9092 kapacitor
```
To use your own kapacitor and its configurations, 
- modify or replace binaries and kapacitor.conf in docker_files/resources