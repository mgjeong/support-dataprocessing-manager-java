#!/bin/bash
echo "Starting flink local cluster..."
exec ${FLINK_HOME}/bin/jobmanager.sh start-foreground local
