#!/bin/sh
echo "Moving runtime common jar"
mv -f ${ENGINE_PATH}/runtime-common.jar ${FW_HA}

echo "Moving runtime task jar"
mv -f ${ENGINE_PATH}/task/task-model-*.jar /

echo "Moving task jars"
mkdir -p ${FW_HA}/jar/task
mkdir -p ${FW_HA}/jar/task_user
mv -f ${ENGINE_PATH}/task/* ${FW_HA}/jar/task/

echo "Starting runtime web server..."
java -jar ${FW_JAR} 
