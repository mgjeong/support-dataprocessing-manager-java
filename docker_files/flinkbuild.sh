#!/bin/bash
cur_path=$(cd "$(dirname "$0")" && pwd)
fw_repo=${cur_path}
output_path=${cur_path}/docker_files/resources

function is_extracted() {
	if [ -f "$1" ]; then
		echo -ne "\033[33m"
		echo -ne "done"
		echo -e "\033[0m"
	else
		echo -ne "\033[31m"
		echo -ne "failed; $1: No such file exists"
		echo -e	"\033[0m"
		exit 404
	fi
	return
}

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
	mv -f flink-${FLINK_VERSION} ${output_path}/flink
}

if [ ! -f "${output_path}/flink/bin/jobmanager.sh" ]; then
	read -p "Do you want to download Flink (yes/no)? " yn
	case $yn in
		[Nn]* ) echo "Please place Flink binaries in ${output_path}/flink.";;
		* ) download_flink;;
	esac
fi

echo -ne "Checking out jobmanager script at ${output_path}/flink/bin..."
is_extracted "${output_path}/flink/bin/jobmanager.sh"

echo "Finished."
