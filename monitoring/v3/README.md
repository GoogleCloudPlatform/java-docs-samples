# Cloud Monitoring Sample

Simple command-line program to demonstrate connecting to the Google
Monitoring API to retrieve API data.

This also includes an example of how to create a cusom metric and
write a TimeSeries value to it.

## Prerequisites to run locally:

    * [Maven 3](https://maven.apache.org)


Go to the [Google Developers Console](https://console.developer.google.com).

    * Go too API Manager -> Credentials
    * Click ['New Credentials', and create a Service Account](https://console.developers.google.com/project/_/apiui/credential/serviceaccount)
     Download the JSON for this service account, and set the
     `GOOGLE_APPLICATION_CREDENTIALS` environment variable to point to the file
     containing the JSON credentials.

    ```
    export GOOGLE_APPLICATION_CREDENTIALS=~/Downloads/<project-id>-0123456789abcdef.json
    ```

# Set Up Your Local Dev Environment

To run locally:
    * `mvn clean install`
    * `./list_resources_example.sh <YOUR-PROJECT-ID>
    * `./run_custom_metrics.sh <YOUR-PROJECT-ID>

## Run Tests

The tests emulate what the scripts accomplish, so there isn't a reason why they
need to be run if the examples work.  However, if you'd like to run them, change
`TEST_PROJECT_ID` in [`ListResourcesTest`](src/test/java/ListResourcesTest.java)
to the appropriate project ID that matches the Service Account pointed to by
`GOOGLE_APPLICATION_CREDENTIALS`, then run:

    mvn test -DskipTests=false

## Contributing changes

See [CONTRIBUTING.md](../../CONTRIBUTING.md).

## Licensing

See [LICENSE](../../LICENSE).


