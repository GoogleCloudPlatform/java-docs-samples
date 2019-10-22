Using the Service Account App Engine Sample
==============================================

Browse Online
-------------

The main code file is [StorageSample.java](https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/storage/xml-api/serviceaccount-appengine-sample/src/main/java/StorageServiceAccountSample.java).

Add Your App Engine Service Account Name to the Project Team
------------------------------------------------------------

See the instructions at https://developers.google.com/storage/docs/xml-api-java-samples
for getting your App Engine Service Account Name and adding it to your project team.

Checkout Instructions
---------------------

**Prerequisites:** install the latest version of [Java](https://java.com) and [Maven](https://maven.apache.org/download.html). You may need to set your `JAVA_HOME`.

You must also be able to work with a GitHub repository (see e.g.,
https://help.github.com/articles/set-up-git).

    cd [someDirectory]
    git clone https://github.com/GoogleCloudPlatform/java-docs-samples.git
    cd java-docs-samples/storage/xml-api/serviceaccount-appengine-sample
    mvn clean package

To enable logging of HTTP requests and responses (highly recommended when
developing), please take a look at logging.properties.

Running and Deploying Your Application from the Command Line
------------------------------------------------------------

To run your application locally on a development server:

    mvn appengine:devserver

To deploy your application to appspot.com:

If this is the first time you are deploying your application to appspot.com, you will to perform the following steps first.

- Go to https://appengine.google.com and create an application.
- Edit src/main/webapp/WEB-INF/appengine-web.xml, and enter the unique application identifier (you chose it in the prior step) between the <application> tags.

If you've done the above, you can deploy at any time:

    mvn appengine:update

If this is the first time you have run "update" on the project, a browser window will open prompting you to log in. Log in with the same Google account the app is registered with.

Set Up a Project in Eclipse
---------------------------

...coming soon...
