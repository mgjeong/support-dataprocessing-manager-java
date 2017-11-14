#!/bin/sh

# Referred to flink-parent/flinkcontrib/docker-flink
# Check out Apache License

# This is entry point of SE docker container.
echo "Moving framework common jar"
mv -f ${ENGINE_PATH}/framework-common.jar ${FW_HA}

echo "Moving framework task jar"
mv -f ${ENGINE_PATH}/task/task-model-*.jar /

echo "Moving task jars"
mkdir -p ${FW_HA}/jar/task
mkdir -p ${FW_HA}/jar/task_user
mv -f ${ENGINE_PATH}/task/* ${FW_HA}/jar/task/

echo "Starting framework web server..."
java -jar ${FW_JAR} &

echo "Starting flink local cluster..."
exec ${FLINK_HOME}/bin/jobmanager.sh start-foreground local

