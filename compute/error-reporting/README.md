# Stackdriver sample for Google Compute Engine
This sample demonstrates how to use [Stackdriver](https://cloud.google.com/error-reporting/) on Google Compute Engine
## Running on Compute Engine
1. Create a compute instance on the Google Cloud Platform Developer's Console
1. SSH into the instance you created
1. Update packages and install required packages
    `sudo apt-get update && sudo apt-get install git-core openjdk-8-jdk maven`
1. Follow the instructions to [Install the Stackdriver Logging Agent](https://cloud.google.com/logging/docs/agent/installation)
1. Create /etc/google-fluentd/config.d/forward.conf and add

    ```xml
    <source>
      type forward
      port 24224
    </source>
    ```

1. Restart the logging agent

    `sudo service google-fluentd restart`
    
1. Clone the repo

    `git clone https://github.com/GoogleCloudPlatform/java-docs-samples.git`

1. Navigate to the Stackdriver sample folder

    `java-docs-samples/compute/stackdriver`

1. Make sure that openjdk 8 is the selected java version

    `sudo update-alternatives --config java`

1. Use maven to package the class as a jar

    `mvn clean package`

1. Switch to the target folder and execute the jar file

    `java -jar compute-stackdriver-1.0-SNAPSHOT-jar-with-dependencies.jar`

1. On the Developer's Console, navigate to Stackdriver Error Reporting and verify that the sample
   error was logged.
