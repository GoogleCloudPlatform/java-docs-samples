# Bigtable proxy

## Overview

A simple server meant to be used as a sidecar to maintain a persistent connection to Bigtable and
collect metrics. The primary purpose is to support applications that can't maintain a longlived
gRPC connection (ie. php in apache).

The proxy is intended to be used as a local sidecar process. The proxy is intended to be shared by
all processes on the VM that it is running on. It's listening address is hardcoded to `localhost`.
The proxy will use [Application Default Credentials](https://cloud.google.com/docs/authentication/application-default-credentials)
for all outbound RPCs.

The proxy will accept local unencrypted connections from Bigtable clients, and:
- attach credentials
- export metrics
- send the RPC over an encrypted channel pool to Bigtable service

## Features

* Metrics - The proxy will track RPC metrics and export them to Google Cloud Monitoring
* Multi tenant - The proxy can be used to connect to many different Bigtable instances
* Credential handling - The proxy has its own set of credentials. It will ignore any inbound
  credentials from the client
* Channel pooling - The proxy will maintain and autosize the outbound channel pool to properly
  load balance RPCs.

## Metrics

The proxy is instrumented with Opentelemtry and will export those metrics to Google Cloud Monitoring
in a project your choosing. The metrics will be published under the namespace
`workload.googleapis.com`. Available metrics:

* `bigtableproxy.server.call.started` The total number of RPCs started, including those that have 
    not completed.
* `bigtableproxy.client.call.credential.duration` Latency of getting credentials
* `bigtableproxy.client.call.queue.duration` Duration of how long the outbound side of the proxy had
  the RPC queued
* `bigtableproxy.client.call.sent_total_message_size` Total bytes sent per call to Bigtable service
  (excluding metadata, grpc and transport framing bytes
* `bigtableproxy.client.call.rcvd_total_message_size` Total bytes received per call from Bigtable 
  service (excluding metadata, grpc and transport framing bytes)
* `bigtableproxy.client.gfe.duration` Latency as measured by Google load balancer from the time it 
  received the first byte of the request until it received the first byte of the response from the
  Cloud Bigtable service.
* `bigtableproxy.client.gfe.duration_missing.count` Count of calls missing gfe response headers
* `bigtableproxy.client.call.duration` Total duration of how long the outbound call took
* `bigtableproxy.client.channel.count` Number of open channels
* `bigtableproxy.client.call.max_outstanding_count` Maximum number of concurrent RPCs in a single
  minute window
* `bigtableproxy.presence` Counts number of proxy processes (emit 1 per process).

## Requirements

* JVM >= 11
* Ensure that the service account includes the IAM roles:
  * `Monitoring Metric Writer`
  * `Bigtable User`
* Ensure that the metrics project has `Stackdriver Monitoring API` enabled

## Expected usage

```sh
# Build the binary
mvn package

# unpack the binary on the proxy host
unzip target/bigtable-proxy-0.0.1-SNAPSHOT-bin.zip
cd bigtable-proxy-0.0.1-SNAPSHOT

# Verify that the proxy has require permissions using an existing table. Please note that the table
# data will not be modified, however a test metric will be written.
./bigtable-verify.sh  \
  --bigtable-project-id=$BIGTABLE_PROJECT_ID \
  --bigtable-instance-id=$BIGTABLE_INSTANCE_ID \
  --bigtable-table-id=$BIGTABLE_TABLE_ID \
  --metrics-project-id=$METRICS_PROJECT_ID

# Then start the proxy on the specified port. The proxy can forward requests for multiple
# Bigtable projects/instances/tables. However it will export health metrics to a single project
# specified by `metrics-project-id`. 
./bigtable-proxy.sh \
  --listen-port=1234 \
  --metrics-project-id=SOME_GCP_PROJECT
 
# Start your application, and redirect the bigtable client to connect to the local proxy. 
export BIGTABLE_EMULATOR_HOST="localhost:1234"
path/to/application/with/bigtable/client
```

## Configuration

Required options:
* `--listen-port=<port>` The local port to listen for Bigtable client connections. This needs to 
  match port in the `BIGTABLE_EMULATOR_HOST="localhost:<port>` environment variable passed to your
  application.
* `--metrics-project-id=<projectid>` The Google Cloud project that should be used to collect metrics
  emitted from the proxy.

Optional configuration:
* The environment variable `GOOGLE_APPLICATION_CREDENTIALS` can be used to use a non-default service
  account. More details can be found here: https://cloud.google.com/docs/authentication/application-default-credentials
