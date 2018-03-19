#!/bin/bash
source $(dirname "$0")/path.prefs

##### Build first #####
$(dirname "$0")/build_local.sh

status=$?
if [ ! $status -eq 0 ]; then
	echo "Build failed."
	exit 1
fi

##### Start Application #####
echo "Starting Application..."
(cd $framework_repo; java -jar ./runtime/target/manager-0.1.0-SNAPSHOT.jar)
