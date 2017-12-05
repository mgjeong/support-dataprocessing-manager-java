#!/bin/bash
source $(dirname "$0")/path.prefs

##### Simple validation #####
#if [ ! -d "$framework_repo" ] || [ ! -d "$flink_bin" ]; then
#	echo "$framework_repo or $flink_bin does not exist."
#	exit 1
#fi

if [ ! -d "$framework_repo/runtime/resource" ]; then
    echo "$framework_repo/runtime/resource does not exist. create directory."
    mkdir $framework_repo/runtime/resource
fi
#if [ ! -d "$flink_bin/../log" ]; then
#    echo "$flink_bin/../log does not exist. create directory."
#    mkdir $flink_bin/../log
#fi
if [ ! -d "$framework_dir" ] || [ ! -d "$framework_dir/jar" ] || [ ! -d "$framework_dir/jar/task" ]; then
	echo "$framework_dir, $framework_dir/jar or $framework_dir/jar/task does not exist. create directory"
    sudo mkdir -p /runtime/ha/jar/task
    sudo mkdir -p /runtime/ha/jar/task_user
    sudo chown $USER:$USER -R /framework
fi

##### Build project #####
echo "Building Data Processing Framework..."
mvn -f $framework_repo/pom.xml clean install package -DskipTests

status=$?
if [ ! $status -eq 0 ]; then
	echo "Build failed."
	exit 1
fi

##### Clear framework_dir #####
echo "Removing DPFW.db..."
rm $framework_dir/DPFW.db*
rm $framework_dir/jar/task/*.jar
rm $framework_dir/jar/task_user/*.jar

##### Copy task models #####
echo "Copying task models..."
find "$framework_repo/runtime-task/" -name \*SNAPSHOT.jar -exec cp {} "$framework_dir/jar/task/" \;
rm $framework_dir/jar/task/task-model-*.jar

echo "Copying engine-flink.jar..."
cp $framework_repo/engine/engine-flink/target/engine-flink-0.1.0-SNAPSHOT-jar-with-dependencies.jar $framework_repo/runtime/resource/engine-flink.jar

echo "Done."
