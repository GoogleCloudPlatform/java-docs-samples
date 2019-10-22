# Java Mailjet Email Sample for Google Compute Engine

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=compute/mailjet/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample demonstrates how to use [Mailjet](https://www.mailjet.com/) on
[Google Compute Engine](https://cloud.google.com/compute/)

See the [sample application documentaion][sample-docs] for more detailed
instructions.

For more information about Mailjet, see their
[documentation](http://dev.mailjet.com/email-api/v3/apikey/).

[sample-docs]: https://cloud.google.com/compute/docs/tutorials/sending-mail/

## Running on Compute Engine

To run the sample, you will need to do the following:

1. [Create a Mailjet Account](https://app.mailjet.com/signup).
1. Create a compute instance on the Google Cloud Platform Developer's Console
1. SSH into the instance you created
1. Update packages and install required packages

    `sudo apt-get update && sudo apt-get install git-core openjdk-8-jdk maven`

1. Clone the repo

    `git clone https://github.com/GoogleCloudPlatform/java-docs-samples.git`

1. Configure your Mailjet settings in the java class (MAILJET_API_KEY, SENDGRID_SENDER)

    `java-docs-samples/compute/mailjet/src/main/java/com/example/compute/mailjet/MailjetSender.java`

1. Navigate back to ./Mailjet and use maven to package the class as a jar

    `mvn clean package`

1. Make sure that openjdk 8 is the selected java version

    `sudo update-alternatives --config java`

1. Execute the jar file with your intended recipient and sender emails as arguments
   and send an email (make sure you are in the target folder)

    `java -jar compute-mailjet-1.0-SNAPSHOT-jar-with-dependencies.jar [RECIPIENT EMAIL] [SENDER EMAIL]`
