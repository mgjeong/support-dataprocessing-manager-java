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
RUN apk add --no-cache bash snappy

# Flink environment variables
ENV FLINK_INSTALL_PATH=/opt
ENV FLINK_HOME $FLINK_INSTALL_PATH/flink
ENV PATH $PATH:$FLINK_HOME/bin

# Variables pointing file paths in the local system

# Install build dependencies and flink
ADD ./docker_files/resources/flink/ $FLINK_HOME
RUN set -x $FLINK_HOME
RUN chmod +x -R ${FLINK_HOME}
COPY "./docker_files/resources/flink-conf.yaml" ${FLINK_HOME}/conf/flink-conf.yaml
COPY run.sh /

# Framework environment variables
ENV FW_PATH /runtime
ENV ENGINE_PATH ${FW_PATH}/resource
ENV FW_JAR /runtime.jar
ENV FW_HA ${FW_PATH}/ha

# Deploy runtime
RUN mkdir -p $ENGINE_PATH/task
RUN mkdir -p $FW_HA/jar/task

EXPOSE 6123 8081-8090

ENTRYPOINT ["/run.sh"]
#CMD ["/bin/bash"]
