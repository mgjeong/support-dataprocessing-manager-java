#!/bin/bash
cur_path=$(cd "$(dirname "$0")" && pwd)
fw_repo="${cur_path}"

function test_x86() {
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
}

arch=$(uname -m)
if [[ "$arch" == *"arm"* ]] || [[ "$arch" == *"ARM"* ]]; then
	echo "Test starts for ARM architecture"
	exit 0
else
	echo "Test starts for x86 architecture"
	test_x86
fi
