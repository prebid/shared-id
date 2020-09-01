#!/usr/bin/env bash
eval $(aws ecr get-login --region us-east-1 --no-include-email)
docker tag shared-id-endpoint:1.1.0 747429369124.dkr.ecr.us-east-1.amazonaws.com/shared-id-endpoint:1.1.0
docker push 747429369124.dkr.ecr.us-east-1.amazonaws.com/shared-id-endpoint:1.1.0