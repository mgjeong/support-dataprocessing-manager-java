#!/bin/bash
cur_path=$(cd "$(dirname "$0")" && pwd)
rsc_path=${cur_path}/docker_files
output_path=${cur_path}/docker_files/resources
source ${rsc_path}/sources.version
mkdir -p $output_path

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

echo "Downloading Kapacitor binaries..."
cd ${output_path}
wget -O kapacitor.tar.gz https://dl.influxdata.com/kapacitor/releases/kapacitor-${KAPACITOR_VERSION}_linux_armhf.tar.gz
tar -xzf kapacitor.tar.gz
rm kapacitor.tar.gz
ln -s "kapacitor-${KAPACITOR_VERSION}"*/ kapacitor
is_extracted ${output_path}/kapacitor/usr/bin/kapacitord

echo -ne "Setting necessary configurations and libraries..."
export GOPATH=${output_path}
go get -u go.uber.org/zap
cp ${rsc_path}/kapacitor.conf ${output_path}/
is_extracted ${output_path}/src/go.uber.org/zap/Makefile
