# Pull Task Queue REST API sample
This sample command line application demonstrates how to access App Engine pull task queues using the Task
Queue REST API and the Google Java API Client Library.

<strong>Important Note:</strong> This sample requires an existing App Engine Pull Task Queue. 
Deploy this 
[sample App Engine pull task queue app](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/appengine/taskqueue/pull) 
to create one first and then come back and run this sample.

## Setup Instructions
1. Create or select a project in the Google Cloud Console:
  1. Visit the [Cloud Console][2]
  1. If this is your first time then click "Create Project," otherwise you can
reuse an existing project by clicking on it.
    1. Note: You will need to enable billing for the project to use Compute
    Engine.

1. Authentication instructions to run the sample (on your local machine or on a Compute Engine VM):
  * Running the sample locally on your development machine:
      1. Install [Google Cloud SDK](https://cloud.google.com/sdk/)
      1. Run the following command to authorize the Cloud SDK and configure your project:
      <pre>gcloud init</pre>
      1. Add your authenticated email account as '&lt;user-email&gt;' and '&lt;writer-email&gt;' elements to the queue.xml 
      file of the App Engine app that created the pull queue task you're trying to access. For more details, please see 
      [Defining Pull Queues](https://cloud.google.com/appengine/docs/java/taskqueue/overview-pull#Java_Defining_pull_queues).
  * Running the sample on a Google Compute Engine VM using Default Application Credentials:
      1. Create a service account and add it to queue.xml
        1. In the API Manager > [Credentials](https://pantheon.corp.google.com/apis/credentials)
        section click "Create credentials" and choose "Service account key".
        1. On the "Create service account key" page, select "Compute Engine default service account" from the "Service Account" drop-down menu. Leave the Key type set to JSON and click the "Create" button.
        1. Once the service account is created, click the "Manage service accounts" link and copy the "Service account ID" of the "Compute Engine default service account".
        1.  Add the "Service account ID" as '&lt;user-email&gt;' and '&lt;writer-email&gt;' elements to the queue.xml file of the 
        App Engine app that created the pull queue task you're trying to access. For more details, please see 
        [Defining Pull Queues](https://cloud.google.com/appengine/docs/java/taskqueue/overview-pull#Java_Defining_pull_queues). 

      1. Create a Compute Engine VM Instance.
        1. In the [Cloud Console](https://console.cloud.google.com/project)
      go to the Compute > Compute Engine section.
        1. Click the "Create instance" button.
        1. For the 'Boot Disk' select a Linux machine image like Debian or Ubuntu.
        1.  In the "Indentity API and Access" section, select "Set access for each API" under the "Access Scopes" subsection and then select the
        "Task queue" drop-down menu to set its scope.
          *  Set the "Task queue" access scope to be "Enabled".
        1. Click the "Create" button.
        1. Once the VM is created click the VM instance's "SSH" button to ssh in to the newly created VM instance.

1. Code checkout instructions:
  1. Prerequisites: install [Java 7 or Java 8 JDK][1], [Git][3], and [Maven][4].
You may need to set your `JAVA_HOME` environment variable as well.
    * To install these prerequisites on a Linux (Debian or Ubuntu) based Compute Engine VM
    instance, run these commands:
    <pre>
    sudo apt-get update
    sudo apt-get install git maven openjdk-7-jdk -y
    </pre>
  1. Download the sample code by running the following commands:
  <pre>mkdir some_directory
  cd some_directory
  git clone https://github.com/GoogleCloudPlatform/java-docs-samples.git
  cd java-docs-samples/taskqueue/cmdline
  </pre>
  In a text editor open the `TaskQueueSample.java` file. For example, to edit the file with nano:
  <pre>nano src/main/java/TaskQueueSample.java</pre>

  1. Specify an 'Application Name' for your app by updating the following line of code:
  <pre>private static final String APPLICATION_NAME = "";</pre>
  1. Save the changes to the file and exit the text editor.

1. Compile and run the sample:
  1. Compile the sample code using Maven by running the following command:
  <pre>mvn compile</pre>
  1. Execute the sample code using Maven by running the following command,
  entering your specific values for the placeholder arguments:
  <pre>mvn -q exec:java -Dexec.args="&lt;ProjectId&gt; &lt;TaskQueueName&gt; &lt;LeaseSeconds&gt; &lt;NumberOfTasksToLease&gt;"</pre>
  1. Running the sample will first list the pull task queue details and then it will lease, process and delete the number of tasks you specified.

      You can verify the details of the pull task queue and the leased tasks by visiting the [App Engine > Task queues](https://pantheon.corp.google.com/appengine/taskqueues) 
      section of the Developers Console.
1. Importing the code into Eclipse and running it from there:
  1. Prerequisites: install [Eclipse][5] and the [Maven plugin for Eclipse][6].
  1. Download code as specified above.
  1. File -> Import -> Maven -> Existing Maven Projects -> Next.
  1. Select your project directory as your "Root Directory," and click "Finish."
  1. Right-click on project task-queue-rest-sample.
  1. Run As > Java Application.
  1. If asked, type or select "TaskQueueSample" and click OK.
  1. Application output will display in the Eclipse Console.

[1]: http://java.com/en/download/faq/develop.xml
[2]: https://console.cloud.google.com/project
[3]: http://git-scm.com/downloads
[4]: http://maven.apache.org/download.html
[5]: http://www.eclipse.org/downloads/
[6]: http://download.eclipse.org/technology/m2e/releases/

