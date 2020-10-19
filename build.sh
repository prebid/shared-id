#!/usr/bin/env bash
mvn clean package
docker build -t shared-id-endpoint:1.0.0 .
