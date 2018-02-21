endpoints-skeleton
==================

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine/endpoints-v1-helloworld/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>


# DEPRECATED

### This sample uses endpoints v1 the preferred strategy for new development is to use Endpoints v2

A skeleton application for Google Cloud Endpoints in Java.

- [App Engine][1]

- [Java][2]

- [Google Cloud Endpoints][3]
- [Google App Engine Maven plugin][4]


1. Update the value of `application` in `appengine-web.xml` to the app
   ID you have registered in the App Engine admin console and would
   like to use to host your instance of this sample.

1. Add your API method to `src/main/java/com/example/helloworld/YourFirstAPI.java`.

1. Optional step: These sub steps are not required but you need this
   if you want to have auth protected methods.

    1. Update the values in `src/main/java/com/example/helloworld/Constants.java`
       to reflect the respective client IDs you have registered in the
       [APIs Console][6].

    1. You also need to supply the web client ID you have registered
       in the [APIs Console][4] to your client of choice (web, Android,
       iOS).

1. Run the application with `mvn appengine:devserver`, and ensure it's
   running by visiting your local server's api explorer's address (by
   default [localhost:8080/_ah/api/explorer][5].)

1. Get the client library with

   $ mvn appengine:endpoints_get_client_lib

   It will generate a client library jar file under the
   `target/endpoints-client-libs/<api-name>/target` directory of your
   project, as well as install the artifact into your local maven
   repository.

1. Deploy your application to Google App Engine with

   $ mvn appengine:update

[1]: https://developers.google.com/appengine
[2]: http://java.com/en/
[3]: https://developers.google.com/appengine/docs/java/endpoints/
[4]: https://developers.google.com/appengine/docs/java/tools/maven
[5]: https://localhost:8080/_ah/api/explorer
[6]: https://console.developers.google.com/
