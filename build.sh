#!/bin/bash
cur_path=$(cd "$(dirname "$0")" && pwd)
fw_repo="${cur_path}/development"

#Maven build
cd $fw_repo
echo "Cleaning framework maven project..."
mvn clean
echo "Starting to build framework..."
mvn install -DskipTests

#Extract jars to the current directory
#1. engine-flink for flink algorithm
#2. framework for web controller
#3. common for common interfaces and classes
#4. jars for tasks
version="$(cat "$fw_repo/pom.xml" | grep -oP '(?<=version>)[^<]+' | head -1)"


function copy_here() {
	echo -n "Extracting $2..."
	cp $1 "${cur_path}/$2"
	is_extracted "${cur_path}/$2"
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

target_fw="${fw_repo}/framework/target/framework-${version}.jar"
target_engine="${fw_repo}/engine-flink/target/engine-flink-${version}-jar-with-dependencies.jar"
target_common="${fw_repo}/framework-common/target/framework-common-${version}.jar"
target_task_parent_dir="${fw_repo}/framework-task/"

if [ ! -f "$target_fw" ] || [ ! -f "$target_engine" ] || [ ! -f "$target_common" ]; then
	echo -e Maven build..."\033[31m"failed"\033[0m"
	echo "; Please check $target_fw, $target_engine, and $target_common"
	exit 403
fi 

copy_here $target_fw "framework.jar"
copy_here $target_engine "engine-flink.jar"
copy_here $target_common "framework-common.jar"

echo "Extracting task jars: "
mkdir -p "${cur_path}/task"
for i in `find $target_task_parent_dir -name \*${version}\*.jar | grep "target/"`
do
	copy_here $i "task/$(basename $i)"
done
echo "...$(ls task | wc -l) task jars are extracted."


read -p "Do you want to download flink?" yn
case $yn in
	[Nn]* ) echo "Please place flink binaries in flink directory"; break;;
	* ) download_flink();;
esac

function download_flink() {
	FLINK_BASE_URL="$(curl -s https://www.apache.org/dyn/closer.cgi\?preferred\=true)flink/flink-${FLINK_VERSION}/"
	FLINK_DIST_FILE_NAME="flink-${FLINK_VERSION}-bin-hadoop${HADOOP_VERSION}-scala_${SCALA_VERSION}.tgz"
	CURL_OUTPUT="flink.tgz"

	echo -ne "Downloading ${FLINK_DIST_FILE_NAME} from ${FLINK_BASE_URL}..."
	cd ${cur_path}
	curl -s ${FLINK_BASE_URL}${FLINK_DIST_FILE_NAME} --output ${CURL_OUTPUT}
	is_extracted "flink.tgz"
	tar -xzf flink.tgz
}

echo -ne "Checking out jobmanager script..."
if [ ! -f "./flink/bin/jobmanager.sh" ]; then
	mv -f flink-${FLINK_VERSION} flink
fi
is_extracted "./flink/bin/jobmanager.sh"

echo "Finished."
