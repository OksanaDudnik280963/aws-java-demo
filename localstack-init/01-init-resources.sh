#!/bin/bash
# Runs automatically when the LocalStack container becomes ready
# (mounted to /etc/localstack/init/ready.d in docker-compose.yml).
# Creates the same bucket + queue names used in application.yml defaults,
# so the app works against LocalStack with zero extra setup.

set -e

echo "Creating S3 bucket: order-attachments"
awslocal s3 mb s3://order-attachments

echo "Creating SQS queue: order-events-queue"
awslocal sqs create-queue --queue-name order-events-queue

echo "LocalStack init complete."
