# Users Authentication sample for Google App Engine

This sample demonstrates how to use the [Users API][appid] on [Google App
Engine][ae-docs].

[appid]: https://cloud.google.com/appengine/docs/java/users/
[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Running locally
This example uses the
[Maven gcloud plugin](https://cloud.google.com/appengine/docs/java/tools/using-maven).
To run this sample locally:

    $ mvn appengine:run

## Deploying
In the following command, replace YOUR-PROJECT-ID with your
[Google Cloud Project ID](https://developers.google.com/console/help/new/#projectnumber).

    $ mvn appengine:deploy
