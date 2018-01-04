Flink
===========
Flink can be connected to Data Processing Manager as a data processing engine.
There is no need to include anything for Apache Flink, but still we provide build scripts to deploy Flink as a container for ease.

## How to build  ##
```shell
$ cd engine/engine-flink
$ ./build.sh
$ sudo docker build -t flink .
```
## How to run  ##
```shell
$ sudo docker run -it -p 6123:6123 -p 8081:8081 flink
```
