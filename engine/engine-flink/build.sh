#!/bin/bash
cur_path=$(cd "$(dirname "$0")" && pwd)
rsc_path=$(dirname $(dirname ${cur_path}))/docker_files
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
	source ${rsc_path}/sources.version
	FLINK_BASE_URL="$(curl -s https://www.apache.org/dyn/closer.cgi\?preferred\=true)flink/flink-${FLINK_VERSION}/"
        FLINK_DIST_FILE_NAME="flink-${FLINK_VERSION}-bin-hadoop${HADOOP_VERSION}-scala_${SCALA_VERSION}.tgz"
        CURL_OUTPUT="flink.tgz"

        echo -ne "Downloading ${FLINK_DIST_FILE_NAME} from ${FLINK_BASE_URL}..."
	mkdir -p ${output_path}
        cd ${output_path}
        curl -s ${FLINK_BASE_URL}${FLINK_DIST_FILE_NAME} --output ${CURL_OUTPUT}
        is_extracted "${CURL_OUTPUT}"
        tar -xzf flink.tgz
        rm flink.tgz
	ln -s "flink-${FLINK_VERSION}" flink

	cp ${rsc_path}/flink-conf.yaml ./
}

if [ ! -f "${output_path}/flink/bin/jobmanager.sh" ]; then
	download_flink
fi

echo -ne "Checking out jobmanager script at ${output_path}/flink/bin..."
is_extracted "${output_path}/flink/bin/jobmanager.sh"

echo "Finished."
