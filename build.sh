#!/bin/bash

# This shell script is to make Docker image of this framework.
cur_path=$(cd "$(dirname "$0")" && pwd)
fw_repo=${cur_path}
output_path=${cur_path}/docker_files/resources

# Maven build
cd $fw_repo
echo "Cleaning framework maven project..."
mvn clean
echo "Starting to build framework..."
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

target_fw="${fw_repo}/runtime/target/framework-${version}.jar"
target_engine="${fw_repo}/engine-flink/target/engine-flink-${version}-jar-with-dependencies.jar"
target_common="${fw_repo}/runtime-common/target/framework-common-${version}.jar"
target_task_parent_dir="${fw_repo}/runtime-task/"

if [ ! -f "$target_fw" ] || [ ! -f "$target_engine" ] || [ ! -f "$target_common" ]; then
	echo -e Maven build..."\033[31m"failed"\033[0m"
	echo "; Please check $target_fw, $target_engine, and $target_common"
	exit 403
fi 

mkdir -p "${output_path}/task"
copy_here $target_fw "framework.jar"
copy_here $target_engine "engine-flink.jar"
copy_here $target_common "framework-common.jar"

echo "Extracting task jars: "
for i in `find $target_task_parent_dir -name \*${version}\*.jar | grep "target/"`
do
	copy_here $i "task/$(basename $i)"
done
echo "...$(ls ${output_path}/task | wc -l) task jars are extracted."

function download_flink() {
	source ${fw_repo}/docker_files/sources.version
	FLINK_BASE_URL="$(curl -s https://www.apache.org/dyn/closer.cgi\?preferred\=true)flink/flink-${FLINK_VERSION}/"
	FLINK_DIST_FILE_NAME="flink-${FLINK_VERSION}-bin-hadoop${HADOOP_VERSION}-scala_${SCALA_VERSION}.tgz"
	CURL_OUTPUT="flink.tgz"

	echo -ne "Downloading ${FLINK_DIST_FILE_NAME} from ${FLINK_BASE_URL}..."
	cd ${cur_path}
	curl -s ${FLINK_BASE_URL}${FLINK_DIST_FILE_NAME} --output ${CURL_OUTPUT}
	is_extracted "flink.tgz"
	tar -xzf flink.tgz
	rm flink.tgz
}

read -p "Do you want to download Flink (yes/no)? " yn
case $yn in
	[Nn]* ) echo "Please place Flink binaries in flink directory.";;
	* ) download_flink;;
esac

echo -ne "Checking out jobmanager script..."
if [ ! -f "./flink/bin/jobmanager.sh" ]; then
	mv -f flink-${FLINK_VERSION} ${output_path}/flink
fi
is_extracted "${output_path}/flink/bin/jobmanager.sh"

echo "Finished."
