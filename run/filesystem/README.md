# Cloud Run File System Sample

This sample shows how to create a service that mounts a Filestore
instance as a network file system.

See our [Using Filestore with Cloud Run tutorial](https://cloud.google.com/run/docs/tutorials/network-filesystems) for instructions for setting up and deploying this sample application.

## Run with [GCS Fuse][fuse]
This sample can also be deployed to use GCS Fuse using the follow instructions. [`gcsfuse`][git] is a user-space file system for interacting with Google Cloud Storage.

### Pre-reqs

1. Install tools:
    * [Docker](https://docs.docker.com/engine/install/ubuntu/)
    * [Cloud SDK](https://cloud.google.com/sdk/docs/install)

    Or use [Cloud Shell](https://cloud.google.com/shell) with tools installed.

1. [Create a GCS bucket][create] or reuse an existing:
    ```bash
    export BUCKET=<YOUR_BUCKET>
    gsutil mb $BUCKET
    ```

1. Set project Id as an environment variable:
    ```bash
    export PROJECT_ID=<YOUR_PROJECT_ID>
    ```

### Ship the code
1. Build the Docker container specifying the Fuse Dockerfile:
    ```bash
    docker build -t gcr.io/$PROJECT_ID/gcsfuse -f gcsfuse.Dockerfile .
    ```

1. Push to Container Registry (See [Setting up authentication for Docker][auth]):
    ```bash
    docker push gcr.io/$PROJECT_ID/gcsfuse
    ```

1. Deploy to Cloud Run:
    ```bash
    gcloud alpha run deploy gcsfuse \
        --image gcr.io/$PROJECT_ID/gcsfuse  \
        --execution-environment gen2 \
        --update-env-vars BUCKET=$BUCKET \
        --allow-unauthenticated
    ```

[create]: https://cloud.google.com/storage/docs/creating-buckets
[fuse]: https://cloud.google.com/storage/docs/gcs-fuse
[git]: https://github.com/GoogleCloudPlatform/gcsfuse
[auth]: https://cloud.google.com/artifact-registry/docs/docker/authentication