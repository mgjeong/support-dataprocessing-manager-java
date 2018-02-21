#!/bin/bash
script_loc=`dirname "$0"`
caller_loc=`pwd`

# shutdown hook
trap 'cd "$caller_loc"' EXIT

echo "Moving to $script_loc..."
cd "$script_loc"

echo "Initializing svace..."
svace init

echo "Building svace..."
#svace build $(dirname "$0")/tools/build_local.sh
svace build mvn clean compile

echo "Analyzing svace..."
svace analyze --warnings $(dirname "$")/warn-settings.java.txt

echo "Uploading svace..."
svace upload --host 10.113.80.59:12000 --module manager --user sysadmin --pass gsmsw2015

echo "Done..."
