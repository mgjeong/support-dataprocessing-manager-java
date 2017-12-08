FROM golang:1.9.2-alpine3.6 AS buildenv

# SEC-specific settings KYUNGRAE KIM
ENV http_proxy 'http://10.112.1.184:8080'
ENV https_proxy 'https://10.112.1.184:8080'

ADD docker_files/resources/SRnD+Web+Proxy.crt /
RUN mv /SRnD+Web+Proxy.crt /etc/ssl/certs/
RUN update-ca-certificates

# Starting to build docker image
RUN apk add --no-cache bash

# Installing packages for EMF
RUN apk update
RUN apk add gcc pkgconfig zeromq-dev
RUN apk add git musl-dev
RUN go get github.com/pebbe/zmq4
RUN go get -u github.com/golang/protobuf/protoc-gen-go
COPY docker_files/resources/src/go.uber.org ${GOPATH}/src/go.uber.org
RUN go build go.uber.org/zap
RUN go install go.uber.org/zap

RUN go get github.com/mgjeong/messaging-zmq/go/emf
ENV UDF_PATH /runtime/ha/go
ENV KAPA_PATH /kapacitor
ENV PATH $PATH:$KAPA_PATH

RUN go get -u github.com/influxdata/kapacitor/udf/agent
RUN mkdir -p $UDF_PATH

ADD inject.go $UDF_PATH
ADD deliver.go $UDF_PATH
RUN go build ${UDF_PATH}/inject.go
RUN go build ${UDF_PATH}/deliver.go

FROM alpine:3.6

ENV http_proxy 'http://10.112.1.184:8080'
ENV https_proxy 'https://10.112.1.184:8080'

RUN apk add --no-cache bash

# Copy EMF-related libraries
COPY --from=buildenv /lib/ld-musl-*.so.* /lib/
COPY --from=buildenv /usr/lib/libzmq.so.* /usr/lib/
COPY --from=buildenv /lib/ld-musl-*.so.* /lib/
COPY --from=buildenv /usr/lib/libsodium.so.* /usr/lib/
COPY --from=buildenv /usr/lib/libstdc++.so.* /usr/lib/
COPY --from=buildenv /usr/lib/libgcc_s.so.* /usr/lib/

# Set configurations
ENV UDF_PATH /runtime/ha/go
ENV KAPA_PATH /kapacitor
ENV PATH $PATH:$KAPA_PATH

RUN mkdir -p $UDF_PATH
RUN mkdir -p $KAPA_PATH

# Copy UDF binaries for EMF
COPY --from=buildenv /go/inject ${UDF_PATH}/
COPY --from=buildenv /go/deliver ${UDF_PATH}/

# ADD kapacitor binaries
ADD docker_files/resources/kapacitor/kapacitor $KAPA_PATH
ADD docker_files/resources/kapacitor/kapacitord $KAPA_PATH

ADD docker_files/resources/kapacitor.conf $KAPA_PATH/
EXPOSE 9092

# Start container at entrypoint
COPY run.sh /
ENTRYPOINT ["/run.sh"]
