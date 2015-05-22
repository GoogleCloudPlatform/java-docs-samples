Using the Service Account Command Line Sample
==============================================================

Browse Online
--------------

The main file is [StorageServiceAccountSample.java](https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/cloud-storage/xml-api/serviceaccount-cmdline-sample/src/main/java/StorageServiceAccountSample.java).

Get a Service Account
---------------------

See the instructions at https://developers.google.com/storage/docs/xml-api-java-samples
for setting up a service account.

Checkout Instructions
---------------------

1. **Prerequisites:**
    1. install the latest version of [Java](http//java.com) and [Maven](http://maven.apache.org/download.html).
    1. [Create](https://cloud.google.com/storage/docs/cloud-console#_creatingbuckets) a Google Cloud Storage bucket
    1. You must also be able to work with [GitHub](https://help.github.com/articles/set-up-git) repositories.
    1. You may need to set your `JAVA_HOME`.

1. Clone repository.

        git clone https://github.com/GoogleCloudPlatform/java-docs-samples.git
        cd java-docs-samples/cloud-storage/xml-api/serviceaccount-cmdline-sample

1. Update key.json file.
1. Compile and run

        mvn compile install
        export GOOGLE_APPLICATION_CREDENTIALS=key.json
        mvn -q exec:java -Dexec.args="your-bucket-name"


To enable logging of HTTP requests and responses (highly recommended when 
developing), please take a look at logging.properties.

Set Up a Project in Eclipse
---------------------------

* **Prerequisites:**
    * Install [Eclipse](http://www.eclipse.org/downloads/), the [Maven plugin](http://eclipse.org/m2e/), and optionally the [GitHub plugin](http://eclipse.github.com/).
    * [Create](https://cloud.google.com/storage/docs/cloud-console#_creatingbuckets) a Google Cloud Storage bucket

* Set up Eclipse Preferences

    * Window > Preferences... (or on Mac, Eclipse > Preferences...)
    * Select Maven

        * check on "Download Artifact Sources"
        * check on "Download Artifact JavaDoc"

* Clone the `serviceaccount-cmdline-sample` to your computer

    * You can use either a plugin with Eclipse or any other tool you normally use
to work with GitHub repositories.

* Create a new project using `serviceaccount-cmdline-sample`

    * Create a new Java Project.
    * Choose the **Location** of the project to be the root directory of the sample you cloned locally.
    * Make sure the Eclipse package name matches the package name used in the
code file `com.google.api.services.samples.storage.serviceaccount.cmdline`.
    * Select the project and **Convert to Maven Project** to add Maven Dependencies.
    * Update the key.json file.
    * Click on Run > Run configurations
        * Navigate to your **Java Application**'s configuration section
        * In the **Arguments** tab, add your bucket name as a **Program argument**
        * In the **Environment** tab, create a variable `GOOGLE_APPLICATION_CREDENTIALS` and set it to `key.json`

* Run

    * Right-click on project
    * Run As > Java Application
    * If asked, type "StorageServiceAccountSample" and click OK
