# Java SendGrid Email Sample for Google Compute Engine

This sample demonstrates how to use [SendGrid](https://www.sendgrid.com) on
[Google Compute Engine](https://cloud.google.com/compute/)

See the [sample application documentaion][sample-docs] for more detailed
instructions.

For more information about SendGrid, see their
[documentation](https://sendgrid.com/docs/User_Guide/index.html).

[sample-docs]: https://cloud.google.com/compute/docs/tutorials/sending-mail/using-sendgrid

## Running on Compute Engine 

To run the sample, you will need to do the following:

1. [Create a SendGrid Account](http://sendgrid.com/partner/google). As of
   September 2015, Google users start with 25,000 free emails per month.
1. Create a compute instance on the Google Cloud Platform Developer's Console
1. SSH into the instance you created
1. Update packages and install required packages
    sudo apt-get update && sudo apt-get install git-core openjdk-8-jdk maven
1. Clone the repo
    git clone https://github.com/GoogleCloudPlatform/java-docs-samples.git
1. Configure your SendGrid settings in the java class (SENDGRID_API_KEY, SENDGRID_SENDER, TO_EMAIL)
    java-docs-samples/compute/sendgrid/src/main/java/com/example/compute/sendgrid/SendEmailServlet.java
1. Navigate back to ./sendgrid and use maven to package the class as a jar
    mvn clean package
1. Switch to the target directory with the jar file and enable execution on that file
    chmod +x compute-sendgrid-1.0-SNAPSHOT-jar-with-dependencies.jar
1. Make sure that openjdk 8 is the selected java version
    sudo update-alternatives --config java
1. Execute the jar file and send an email (make sure you are in the target folder)
    java -jar compute-sendgrid-1.0-SNAPSHOT-jar-with-dependencies.jar

