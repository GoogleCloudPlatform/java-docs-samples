# Google Cloud Run Java Samples

[![Open in Cloud Shell][shell_img]][shell_link]

[shell_img]: http://gstatic.com/cloudssh/images/open-btn.png
[shell_link]: https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=blog/README.md

This directory contains samples for [Google Cloud Run](https://cloud.run). [Cloud Run][run_docs] runs stateless [containers](https://cloud.google.com/containers/) on a fully managed environment or in your own GKE cluster.

## Samples

|           Sample                |        Description       |     Deploy    |
| ------------------------------- | ------------------------ | ------------- |
|[Hello World][helloworld]&nbsp;&#10149; | Quickstart | [<img src="https://storage.googleapis.com/cloudrun/button.svg" alt="Run on Google Cloud" height="30">][run_button_helloworld] |
|[Cloud Pub/Sub][pubsub] | Handling Pub/Sub push messages | [<img src="https://storage.googleapis.com/cloudrun/button.svg" alt="Run on Google Cloud" height="30">][run_button_pubsub] |

For more Cloud Run samples beyond Java, see the main list in the [Cloud Run Samples repository](https://github.com/GoogleCloudPlatform/cloud-run-samples).

## Setup

1. [Set up for Cloud Run development](https://cloud.google.com/run/docs/setup)

2. Clone this repository:

    ```
    git clone https://github.com/GoogleCloudPlatform/java-docs-samples.git
    ```

    Note: Some samples in the list above are hosted in other repositories. They are noted with the symbol "&#10149;".


## How to run a sample locally

1. [Install docker locally](https://docs.docker.com/install/)

2. [Build the sample container](https://cloud.google.com/run/docs/building/containers#building_locally_and_pushing_using_docker):

    ```
    export SAMPLE=<SAMPLE_NAME>
    cd $SAMPLE
    docker build --tag $SAMPLE .
    ```

3. [Run containers locally](https://cloud.google.com/run/docs/testing/local)

    With the built container:

    ```
    PORT=8080 && docker run --rm -p 8080:${PORT} -e PORT=${PORT} $SAMPLE
    ```

    Overriding the built container with local code:

    ```
    PORT=8080 && docker run --rm \
        -p 8080:${PORT} -e PORT=${PORT} \
        -v $PWD:/app $SAMPLE
    ```

    Injecting your service account key:

    ```
    export SA_KEY_PATH=/path/to/service/account/my-key-name-123.json

    PORT=8080 && docker run --rm \
        -p 8080:${PORT} -e PORT=${PORT} \
        -e GOOGLE_APPLICATION_CREDENTIALS=$SA_KEY_PATH \
        -v $GOOGLE_APPLICATION_CREDENTIALS:$SA_KEY_PATH:ro \
        -v $PWD:/app $SAMPLE
    ```

## Deploying

1. Set your GCP Project ID as an environment variable:
```
export GOOGLE_CLOUD_PROJECT=<PROJECT_ID>
```

2. Choose to push your image to Google Container Registry or submit a build to
Cloud Build:
  *  Push the docker build to Google Container Registry:
  ```
  docker tag $SAMPLE gcr.io/${GOOGLE_CLOUD_PROJECT}/${SAMPLE}
  docker push gcr.io/${GOOGLE_CLOUD_PROJECT}/${SAMPLE}
  ```
  Learn more about [pushing and pulling images][push-pull].

  * Submit a build using Google Cloud Build:
  ```
  gcloud builds submit --tag gcr.io/${GOOGLE_CLOUD_PROJECT}/${SAMPLE}
  ```

3. Deploy to Cloud Run:
```
gcloud beta run deploy $SAMPLE \
  --set-env-vars GOOGLE_CLOUD_PROJECT=${GOOGLE_CLOUD_PROJECT}  \
  --image gcr.io/${GOOGLE_CLOUD_PROJECT}/${SAMPLE}
```

## Next Steps
* See [building containers][run_build] and [deploying container images][run_deploy]
  for more information.

* [Dockerize a Spring Boot app][jib-tutorial] without a Dockerfile using [Jib][jib].

* To improve [Tomcat startup time][startup], add
  `-Djava.security.egd=file:/dev/./urandom` to the Dockerfile's `CMD`
  instructions. This is acceptable for many applications. However, if your
  application does it's own key generation, such as for SSL, and you require
  greater security, you may prefer to not set `java.security.egd`.


[run_docs]: https://cloud.google.com/run/docs/
[run_build]: https://cloud.google.com/run/docs/building/containers
[run_deploy]: https://cloud.google.com/run/docs/deploying
[helloworld]: https://github.com/knative/docs/tree/master/docs/serving/samples/hello-world/helloworld-java
[pubsub]: pubsub/
[run_button_helloworld]: https://console.cloud.google.com/cloudshell/editor?shellonly=true&cloudshell_image=gcr.io/cloudrun/button&cloudshell_git_repo=https://github.com/knative/docs&cloudshell_working_dir=docs/serving/samples/hello-world/helloworld-java
[run_button_pubsub]: https://console.cloud.google.com/cloudshell/editor?shellonly=true&cloudshell_image=gcr.io/cloudrun/button&cloudshell_git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&cloudshell_working_dir=run/cloudrun-pubsub
[push-pull]: https://cloud.google.com/container-registry/docs/pushing-and-pulling
[jib]: https://github.com/GoogleContainerTools/jib
[jib-tutorial]: https://github.com/GoogleContainerTools/jib/tree/master/examples/spring-boot
[startup]: https://cwiki.apache.org/confluence/display/TOMCAT/HowTo+FasterStartUp
