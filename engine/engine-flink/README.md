Flink
===========
Flink can be connected to Data Processing Runtime as a data processing engine.
There is no need to include anything for Flink, but still we provide build scripts to deploy Flink as a container for ease.

### Deploying as a Docker container ###
```
cd engine/engine-flink
./build.sh
sudo docker build -t flink .
sudo docker run -it -p 6123:6123 -p 8081:8081 flink
```
