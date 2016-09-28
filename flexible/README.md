# Google App Engine Flexible Environment Java Samples

This is a repository that contains Java code samples for [Google App Engine
flexible environment][aeflex-docs].

See our other [Google Cloud Platform GitHub
repos](https://github.com/GoogleCloudPlatform) for sample applications and
scaffolding for other frameworks and use cases.

[aeflex-docs]: https://cloud.google.com/appengine/docs/flexible/

## Run Locally
1. Install the [Google Cloud SDK](https://cloud.google.com/sdk/), including the [gcloud tool](https://cloud.google.com/sdk/gcloud/), and [gcloud app component](https://cloud.google.com/sdk/gcloud-app).
1. Setup the gcloud tool.

   ```
   gcloud init
   ```

1. Clone this repo.

   ```
   git clone https://github.com/GoogleCloudPlatform/java-docs-samples.git
   ```

1. Run this project locally from the command line.

   ```
   mvn clean jetty:run
   ```

1. Visit the application at [http://localhost:8080](http://localhost:8080).

## Deploying

1. Use the [Cloud Developer Console](https://console.developer.google.com)  to create a project/app id. (App id and project id are identical)
1. Setup the gcloud tool.

   ```
   gcloud init
   ```
1. Use the [Admin Console](https://appengine.google.com) to view data, queues, and other App Engine specific administration tasks.
1. Use gcloud to deploy your app.

   ```
   mvn clean gcloud:deploy
   ```

1. Congratulations!  Your application is now live at your-app-id.appspot.com

## Contributing changes

* See [CONTRIBUTING.md](CONTRIBUTING.md)

## Licensing

* See [LICENSE](LICENSE)
