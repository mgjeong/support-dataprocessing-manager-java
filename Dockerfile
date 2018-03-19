###############################################################################
# Copyright 2017 Samsung Electronics All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
###############################################################################
FROM openjdk:8-jdk-alpine

# Install requirements
RUN apk add --no-cache snappy
RUN apk --update add bash gcc make perl libc-dev

# Variables pointing file paths in the local system
# Install build dependencies and flink
COPY run.sh /

# Framework environment variables
ENV FW_PATH /runtime
ENV FW_HA ${FW_PATH}/ha
ENV ENGINE_PATH ${FW_HA}/resource
ENV FW_JAR /manager.jar

# Deploy runtime
RUN mkdir -p $ENGINE_PATH/task
RUN mkdir -p $FW_HA/jar/task

COPY ./docker_files/resources/manager.jar /
COPY ./docker_files/resources/engine-flink.jar $ENGINE_PATH
COPY ./docker_files/resources/manager-common.jar $ENGINE_PATH
COPY ./docker_files/resources/task $ENGINE_PATH/task
EXPOSE 8082 8083

ENTRYPOINT ["/run.sh"]

