# Google Cloud CDN - Signing URLs
Java implementation of  [`gcloud alpha compute sign-url`](https://cloud.google.com/sdk/gcloud/reference/alpha/compute/sign-url)
- uses a private key to create a time-sensitive URL that can be used to access a private Cloud CDN endpoint
- requires [random 128-bit key](https://cloud.google.com/cdn/docs/signed-urls#creatingkeys) encoded as base64 and [uploaded to a backend bucket](https://cloud.google.com/sdk/gcloud/reference/alpha/compute/backend-buckets/add-signed-url-key)

## Getting Started

1. [Download](https://maven.apache.org/download.cgi) and [install](https://maven.apache.org/install.html)  maven to handle the project's dependencies
2. run `mvn clean verify` to build the project and run the tests
