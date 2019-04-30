# Publish messages to Google Cloud Pub/Sub from mobile/client applications

This is not an officially supported Google product.

Copyright 2019 Google LLC

## Introduction
This repository provides sample implementation of a proxy service which enables client-side apps to publish messages to Google Cloud [Pub/Sub](https://cloud.google.com/pubsub/docs/overview). The proxy service:
- Authenticates incoming end user requests. 
- Forwards authenticated requests to Pub/Sub using appropriate [Cloud IAM](https://cloud.google.com/iam/docs/overview) permissions

The detailed steps to run this proxy on GCP is covered in the tutorial available [here]().

## Google Cloud Products Used or Referenced:
- Cloud PubSub
- Compute Engine
- Cloud Build 
- Cloud Endpoints
- Container Registry

## Local Deployment & Testing
For production grade deployments, refer to the [detailed solution post]().

Clone repository:
```
git clone https://github.com/GoogleCloudPlatform/solutions-pubsub-proxy-rest
cd solutions-pubsub-proxy-rest
```
Set environment variables:
```
export SERVICE_ACCOUNT_NAME=proxy-test-sa
export SERVICE_ACCOUNT_DEST=sa.json
export TOPIC=test-topic
export GOOGLE_APPLICATION_CREDENTIALS=$(pwd)/sa.json
export PROJECT=$(gcloud info --format='value(config.project)')
```
Create Pub/Sub topic:
```
gcloud pubsub topics create $TOPIC
```
Create service account:
```
gcloud iam service-accounts create \
   $SERVICE_ACCOUNT_NAME \
   --display-name $SERVICE_ACCOUNT_NAME

SA_EMAIL=$(gcloud iam service-accounts list \
   --filter="displayName:$SERVICE_ACCOUNT_NAME" \
   --format='value(email)')

gcloud projects add-iam-policy-binding $PROJECT \
   --member serviceAccount:$SA_EMAIL \
   --role roles/pubsub.publisher

mkdir -p $(dirname $SERVICE_ACCOUNT_DEST) && \
gcloud iam service-accounts keys create \
   $SERVICE_ACCOUNT_DEST \
   --iam-account $SA_EMAIL
```
### Run Proxy Without Containerizing
To execute test cases and package, run:
```
mvn clean compile assembly:assembly package
```
To skip test cases, run:
```
mvn clean compile assembly:assembly package -DskipTests
```
On a new terminal, start the proxy after changing to the directory where pubsub-proxy was cloned:
```
export GOOGLE_APPLICATION_CREDENTIALS=$(pwd)/sa.json
java -jar target/pubsub-proxy-0.0.1-SNAPSHOT-jar-with-dependencies.jar 
```
Back on the original terminal, publish a message to Cloud Pub/Sub via proxy.
```
curl -i -X POST localhost:8080/publish \
   -H "Content-Type: application/json" \
   -d '{"topic": "'$TOPIC'", "messages": [ {"attributes": {"key1": "value1", "key2" : "value2"}, "data": "test data"}]}'
```
On the terminal running the proxy, check the logs to verify if the message was successfully published to Pub/Sub.

### Deploy Proxy on GKE
Detailed steps to run this proxy on GCP is covered in the tutorial [here]().

## Cleaning Up
Remove the private key:
```
rm -rf $SERVICE_ACCOUNT_DEST
```
