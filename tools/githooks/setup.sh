#!/bin/bash

scriptPath=$(dirname $(realpath "$0"))

checkStyleJar="$scriptPath/checkstyle/checkstyle-8.1.jar"
checkStyleCheckFile="$scriptPath/checkstyle/google_checks.xml"

preHookSrc="$scriptPath/pre-commit"
preHookDest="$scriptPath/../../.git/hooks/pre-commit"

echo "scriptPath: $scriptPath"
echo "checkStyleJar: $checkStyleJar"
echo "checkStyleCheckFile: $checkStyleCheckFile"
echo "preHookSrc: $preHookSrc"
echo "preHookDest: $preHookDest"

echo "Creating symbolic link in .git/hooks/pre-commit"
ln -s -f ${preHookSrc} ${preHookDest}

echo "Setting up git config for checkstyle"
git config --add checkstyle.jar ${checkStyleJar}
git config --add checkstyle.checkfile ${checkStyleCheckFile}

echo "Done";
