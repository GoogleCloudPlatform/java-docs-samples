# appengine/guestbook-cloud-datastore

An App Engine guestbook using Java, Maven, and the Cloud Datastore API via
[google-cloud-java](https://github.com/GoogleCloudPlatform/google-cloud-java).

Please ask questions on [StackOverflow](http://stackoverflow.com/questions/tagged/google-app-engine).

## Running Locally

First, pick a project ID. You can create a project in the [Cloud Console] if you'd like, though this
isn't necessary unless you'd like to deploy the sample.

Second, modify `Persistence.java`: replace `your-project-id-here` with the project ID you picked.

Then start the [Cloud Datastore Emulator](https://cloud.google.com/datastore/docs/tools/datastore-emulator):

    gcloud beta emulators datastore start --project=YOUR_PROJECT_ID_HERE

Finally, in a new shell, [set the Datastore Emulator environmental variables](https://cloud.google.com/datastore/docs/tools/datastore-emulator#setting_environment_variables)
and run

    mvn clean appengine:run

## Deploying

Modify `appengine-web.xml` to reflect your app ID and version, then:

    mvn clean appengine:deploy
