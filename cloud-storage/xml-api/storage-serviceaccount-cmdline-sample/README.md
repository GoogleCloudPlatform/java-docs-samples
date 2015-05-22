Using the Service Account Command Line Sample
==============================================================

Browse Online
--------------

The main file is [StorageServiceAccountSample.java](https://github.com/GoogleCloudPlatform/cloud-storage-docs-xml-api-examples/blob/master/storage-serviceaccount-cmdline-sample/src/main/java/StorageServiceAccountSample.java).

Get a Service Account
---------------------

See the instructions at https://developers.google.com/storage/docs/xml-api-java-samples
for setting up a service account.

Checkout Instructions
---------------------

**Prerequisites:** install the latest version of [Java](http//java.com) and [Maven](http://maven.apache.org/download.html). You must also be able to work with qcGitHub repository (see e.g.,
https://help.github.com/articles/set-up-git).

You may need to set your `JAVA_HOME`.

1. Clone repository.

        cd [someDirectory]
        git clone https://github.com/GoogleCloudPlatform/cloud-storage-docs-xml-api-examples.git
        cd cloud-storage-docs-xml-api-examples/storage-serviceaccount-cmdline-sample

2. Update key.p12 file.
3. Edit `StorageServiceAccountSample.java` to add your bucket and service account email.
4. Compile and run

        mvn compile install
        mvn -q exec:java


To enable logging of HTTP requests and responses (highly recommended when 
developing), please take a look at logging.properties.

Set Up a Project in Eclipse
---------------------------

**Prerequisites:** install [Eclipse](http://www.eclipse.org/downloads/), the [Maven plugin](http://m2eclipse.sonatype.org/installing-m2eclipse.html), and optionally the 
[GitHub plugin](http://eclipse.github.com/).

* Set up Eclipse Preferences

    * Window > Preferences... (or on Mac, Eclipse > Preferences...)
    * Select Maven
        
        * check on "Download Artifact Sources"
        * check on "Download Artifact JavaDoc"

* Clone the `storage-serviceaccount-cmdline-sample` to your computer

    * You can use either a plugin with Eclipse or any other tool you normally use
to work with GitHub repositories.

* Create a new project using `storage-serviceaccount-cmdline-sample`

    * Create a new Java Project.
    * Choose the **Location** of the project to be the root directory of the sample you cloned locally.
    * Make sure the Eclipse package name matches the package name used in the
code file `com.google.api.services.samples.storage.serviceaccount.cmdline`.
    * Select the project and **Convert to Maven Project** to add Maven Dependencies.
    * Edit `StorageServiceAccountSample.java` and specify your SERVICE_ACCOUNT_EMAIL and
BUCKET_NAME.
    * Update the key.p12 file.

* Run

    * Right-click on project
    * Run As > Java Application
    * If asked, type "StorageServiceAccountSample" and click OK
