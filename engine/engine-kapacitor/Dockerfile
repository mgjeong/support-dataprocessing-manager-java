FROM ubuntu:16.04 AS buildenv

RUN apt-get update
RUN apt-get install -y wget

RUN echo "deb http://download.opensuse.org/repositories/network:/messaging:/zeromq:/release-stable/Debian_9.0/ ./" >> /etc/apt/sources.list
RUN wget --no-check-certificate -q https://download.opensuse.org/repositories/network:/messaging:/zeromq:/release-stable/Debian_9.0/Release.key -O- | apt-key add
RUN apt-get update
RUN apt-get install -y gcc git pkg-config
RUN apt-get install -y libzmq3-dev

ENV DEBIAN_FRONTEND noninteractive
ENV INITRD No
ENV LANG en_US.UTF-8
ENV GOVERSION 1.9.1
ENV GOROOT /opt/go
ENV GOPATH /root/.go

RUN cd /opt && wget --no-check-certificate -q https://storage.googleapis.com/golang/go${GOVERSION}.linux-amd64.tar.gz && \
    tar zxf go${GOVERSION}.linux-amd64.tar.gz && rm go${GOVERSION}.linux-amd64.tar.gz && \
    ln -s /opt/go/bin/go /usr/bin/ && \
    mkdir $GOPATH

RUN go get github.com/pebbe/zmq4
RUN go get -u github.com/golang/protobuf/protoc-gen-go
COPY docker_files/resources/src/go.uber.org ${GOPATH}/src/go.uber.org
RUN go build go.uber.org/zap
RUN go install go.uber.org/zap

RUN go get github.com/mgjeong/protocol-ezmq-go/ezmq
ENV UDF_PATH /runtime/ha/go
ENV KAPA_PATH /kapacitor
ENV PATH $PATH:$KAPA_PATH

RUN go get -u github.com/influxdata/kapacitor/udf/agent
COPY docker_files/resources/src/gopkg.in/ ${GOPATH}/src/gopkg.in
RUN mkdir -p $UDF_PATH

ADD src/deliver $GOPATH/src/deliver
ADD src/inject $GOPATH/src/inject
RUN ls $GOPATH/src/
WORKDIR $GOPATH
COPY docker_files/resources/setldd.sh ./
RUN go build src/inject/inject.go
RUN go build src/deliver/deliver.go
RUN mkdir -p /dependencies
RUN ./setldd.sh inject /dependencies/
RUN ./setldd.sh deliver /dependencies2/
RUN ldd inject
RUN ldd deliver

FROM ubuntu:16.04 

RUN apt-get update
RUN apt-get install -y pkg-config

# Copy EMF-related libraries
COPY --from=buildenv /dependencies/* /dependencies/
RUN mv -f /dependencies/* /usr/lib/x86_64-linux-gnu/

# Set configurations
ENV UDF_PATH /runtime/ha/go
ENV KAPA_PATH /kapacitor
ENV PATH $PATH:$KAPA_PATH
RUN mkdir -p $UDF_PATH
RUN mkdir -p $KAPA_PATH

# Copy UDF binaries for EMF
COPY --from=buildenv /root/.go/inject ${UDF_PATH}/
COPY --from=buildenv /root/.go/deliver ${UDF_PATH}/

# ADD kapacitor binaries
ADD docker_files/resources/kapacitor/kapacitor $KAPA_PATH
ADD docker_files/resources/kapacitor/kapacitord $KAPA_PATH

ADD docker_files/resources/kapacitor.conf $KAPA_PATH/
EXPOSE 9092

# Start container at entrypoint
COPY run.sh /
ENTRYPOINT ["/run.sh"]
