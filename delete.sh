#!/usr/bin/env bash

aws --profile nva-dev cloudformation delete-stack --stack-name test-lambda --region eu-west-1
