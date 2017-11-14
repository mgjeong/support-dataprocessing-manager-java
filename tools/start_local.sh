#!/bin/bash
source $(dirname "$0")/path.prefs

##### Simple validation #####
if [ ! -d "$flink_bin" ]; then
	echo "$flink_bin does not exist."
	exit 1
fi

##### Build first #####
$(dirname "$0")/build_local.sh

status=$?
if [ ! $status -eq 0 ]; then
	echo "Build failed."
	exit 1
fi

##### Restart Flink #####
echo "Stopping Apache Flink cluster..."
$flink_bin/stop-cluster.sh

echo "Removing Apache Flink logs..."
rm $flink_bin/../log/*

echo "Starting Apache Flink cluster..."
$flink_bin/start-cluster.sh

# trap ctrl-c 
trap ctrl_c INT

function ctrl_c() {
	echo "Stopping Apache Flink cluster..."
	$flink_bin/stop-cluster.sh
}

##### Start Application #####
echo "Starting Application..."
(cd $framework_repo; java -jar ./runtime/target/runtime-0.1.0-SNAPSHOT.jar)

