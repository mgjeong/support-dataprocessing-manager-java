#!/bin/bash
cur_path=$(cd "$(dirname "$0")" && pwd)
fw_repo="${cur_path}"

cd $fw_repo
mvn test
status=$?
if [ $status -eq 0 ]; then
	echo "Unit tests are done."
	exit 0
else
	echo "Unit test is failed."
	exit 1
fi
