#!/bin/bash
# This is to clear files used for docker image
#FLINK_NAME="./flink.tgz"
FW_NAME="./runtime.jar"
ENGINE_NAME="./engine-flink.jar"
COMMON_NAME="./runtime-common.jar"
STORAGE_NAME="./temp_storage_data"
#if [ -f $FLINK_NAME ]; then
#	rm $FLINK_NAME
#fi
if [ -f $FW_NAME ]; then
	rm $FW_NAME
fi
if [ -f $ENGINE_NAME ]; then
	rm $ENGINE_NAME
fi
if [ -f $COMMON_NAME ]; then
	rm $COMMON_NAME
fi
if [ -f $STORAGE_NAME ]; then
	rm $STORAGE_NAME
fi
rm -rf ha
rm -rf task
