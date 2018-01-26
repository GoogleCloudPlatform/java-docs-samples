# SparkJava on App Engine Flexible Environment

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=flexible/sparkjava/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This app demonstrates how to use [Datastore with the Google Cloud client
library](https://github.com/GoogleCloudPlatform/google-cloud-java/tree/master/google-cloud-datastore)
from within an [App Engine flexible
environment](https://cloud.google.com/appengine/docs/flexible/java/hello-world)
project using [SparkJava](http://sparkjava.com/). The app allows you to create
and modify a database of "users", which contains their ID, name, and email
information.

The Google Cloud client library is an idiomatic Java client for [Google Cloud
Platform](https://cloud.google.com/) services. Read more about the library
[here](https://github.com/GoogleCloudPlatform/google-cloud-java).

Setup
-----

1.  Create a Google Cloud project with the Datastore API enabled.
    [Follow these
    instructions](https://cloud.google.com/docs/authentication#preparation) to
    get your project set up. If you wish to deploy this application, you will
    also need to [enable
    billing](https://support.google.com/cloud/?rd=2#topic=6288636).

2. Set up the local development environment by [installing the Google Cloud
   SDK](https://cloud.google.com/sdk/) and running the following commands in
   command line: `gcloud auth application-default login` and `gcloud config set project [YOUR
   PROJECT ID]`.

3. Ensure that you have Maven installed and configured to use Java 8. See
   installation instructions [here](https://maven.apache.org/install.html).

Running locally
---------------

Run the application on your local machine by typing the following into your
command line from the `sparkjava` directory: `mvn clean package exec:java`.
Navigate to `localhost:8080` to view and interact with the application.

Deploying
---------

If you've enabled billing (step 1 in [Setup](#Setup)), you can deploy the
application to the web by running `mvn appengine:deploy` from your command line
(from the `sparkjava` directory).

How does it work?
-----------------

You'll notice that the source code is split into three folders: `appengine`,
`java/com/google/appengine/sparkdemo`, and `resource/public`. The `appengine`
folder contains a `Dockerfile` and an `app.yaml`, necessary files to [configure
the VM
environment](https://cloud.google.com/appengine/docs/managed-vms/config). The
`java/com/google/appengine/sparkdemo` folder contains the controller code,
which uses the Google Cloud client library to modify the records in the Google Cloud
Datastore. Finally, the `resource/public` folder contains the home webpage,
which uses jQuery to send HTTP requests to create, remove, and update records.

Spark runs the [`main`
method](https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/managedvms/sparkjava-demo/src/main/java/com/google/appengine/sparkdemo/Main.java)
upon server startup. The `main` method creates the controller,
[`UserController`](https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/managedvms/sparkjava-demo/src/main/java/com/google/appengine/sparkdemo/UserController.java).
The URIs used to send HTTP requests in the [home
page](https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/managedvms/sparkjava-demo/src/main/resources/public/index.html)
correspond to methods in the `UserController` class. For example, the
`index.html` code for `create` makes a `POST` request to the path `/api/users`
with a body containing the name and email of a user to add. `UserController`
contains the following code to process that request:

```java
post("/api/users", (req, res) -> userService.createUser(
    req.queryParams("name"),
    req.queryParams("email),
), json());
```
This code snippet gets the name and email of the user from the POST request and
passes it to `createUser` (in
[`UserService.java`](https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/managedvms/sparkjava-demo/src/main/java/com/google/appengine/sparkdemo/UserService.java))
to create a database record using the Google Cloud client library. If you want
a more in-depth tutorial on using Google Cloud client library Datastore client,
see the [Getting
Started](https://github.com/GoogleCloudPlatform/google-cloud-java/tree/master/google-cloud-datastore#getting-started)
section of the client library documentation.

Communication with the Google Cloud Datastore requires authentication and
setting a project ID. When running locally, the Google Cloud client library
automatically detects your credentials and project ID because you logged into
the Google Cloud SDK and set your project ID. There are also many other options
for authenticating and setting a project ID. To read more, see the
[Authentication](https://github.com/GoogleCloudPlatform/google-cloud-java#authentication)
and [Specifying a Project
ID](https://github.com/GoogleCloudPlatform/google-cloud-java#specifying-a-project-id)
sections of the client library documentation.

You built and ran this application using Maven. To read more about using Maven
with App Engine flexible environment, see the [Using Apache Maven
documentation](https://cloud.google.com/appengine/docs/flexible/java/using-maven).
While this particular project uses Maven, the Google Cloud client library
packages can also be accessed using Gradle and SBT.  See how to obtain the
dependency in the [Quickstart
section](https://github.com/GoogleCloudPlatform/google-cloud-java#quickstart)
of the client library documentation.

License
-------

Apache 2.0 - See
[LICENSE](https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/LICENSE)
for more information.
