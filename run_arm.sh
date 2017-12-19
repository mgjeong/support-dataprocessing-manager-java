#!/bin/bash
echo "Starting Kapacitor"
${KAPA_PATH}/kapacitord run -config ${KAPA_PATH}/kapacitor.conf
