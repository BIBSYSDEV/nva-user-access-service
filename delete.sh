#!/usr/bin/env bash

aws --profile default cloudformation delete-stack --stack-name test-lambda --region eu-west-1
