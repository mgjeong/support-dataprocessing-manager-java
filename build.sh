#!/bin/bash

# This shell script is to make Docker image of this data processing runtime.
cur_path=$(cd "$(dirname "$0")" && pwd)
fw_repo=${cur_path}
output_path=${cur_path}/docker_files/resources

# Maven build
cd $fw_repo
echo "Cleaning maven projects..."
mvn clean
echo "Starting to build projects..."
mvn install -DskipTests

# Extract jars to the current directory
# 1. engine-flink for flink algorithm
# 2. framework for web controller
# 3. common for common interfaces and classes
# 4. jars for tasks
version="$(cat "$fw_repo/pom.xml" | grep -oP '(?<=version>)[^<]+' | head -1)"


function copy_here() {
	echo -n "Extracting $2..."
	cp $1 "${output_path}/$2"
	is_extracted "${output_path}/$2"
	return
}

function is_extracted() {
	if [ -f "$1" ]; then
		echo -ne "\033[33m"
		echo -ne "done"
		echo -e "\033[0m"
	else
		echo -ne "\033[31m"
		echo -ne "failed; $1: No such file exists"
		echo -e "\033[0m"
		exit 404
	fi
	return
}

target_fw="${fw_repo}/runtime/target/runtime-${version}.jar"
target_engine="${fw_repo}/engine/engine-flink/target/engine-flink-${version}-jar-with-dependencies.jar"
target_common="${fw_repo}/runtime-common/target/runtime-common-${version}.jar"
target_task_parent_dir="${fw_repo}/runtime-task/"

if [ ! -f "$target_fw" ] || [ ! -f "$target_engine" ] || [ ! -f "$target_common" ]; then
	echo -e Maven build..."\033[31m"failed"\033[0m"
	echo "; Please check $target_fw, $target_engine, and $target_common"
	exit 403
fi 

mkdir -p "${output_path}/task"
copy_here $target_fw "runtime.jar"
copy_here $target_engine "engine-flink.jar"
copy_here $target_common "runtime-common.jar"

echo "Extracting task jars: "
for i in `find $target_task_parent_dir -name \*${version}\*.jar | grep "target/"`
do
	copy_here $i "task/$(basename $i)"
done
echo "...$(ls ${output_path}/task | wc -l) task jars are extracted."

echo "Finished."
