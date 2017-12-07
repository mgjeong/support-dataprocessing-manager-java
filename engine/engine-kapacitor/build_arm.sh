#!/bin/bash
cur_path=$(cd "$(dirname "$0")" && pwd)
rsc_path=$(dirname $(dirname ${cur_path}))/docker_files
output_path=${cur_path}/docker_files/resources
source ${rsc_path}/sources.version
mkdir -p $output_path

echo -ne "Downloading Kapacitor binaries..."
cd ${output_path}
wget -O kapacitor.tar.gz https://dl.influxdata.com/kapacitor/releases/kapacitor-${KAPACITOR_VERSION}_linux_armhf.tar.gz
tar -xzf kapacitor.tar.gz
rm kapacitor.tar.gz
ln -s "kapacitor-${KAPACITOR_VERSION}"*/ kapacitor

echo "Setting necessary configurations and libraries..."
export GOPATH=${output_path}
go get -u go.uber.org/zap
cp ${rsc_path}/kapacitor.conf ${output_path}/

# Temporarily, a certificate is required for proxy
sudo cp /root/SRnD+Web+Proxy.crt ${output_path}/
cp /usr/bin/qemu-arm-static ${output_path}/
