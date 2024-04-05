#!/usr/bin/env bash

export SERVICE_NAME=config
export SERVICE_VER=1.0.7

if [ "$1" = "up" ]; then
    mvn -pl ${SERVICE_NAME}-server -P+layer -am clean package
fi

if [ -z "$1" ]; then
    exec docker compose up
else
    exec docker compose "$@"
fi
