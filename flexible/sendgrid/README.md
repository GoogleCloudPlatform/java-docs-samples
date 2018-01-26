# Java SendGrid Email Sample for Google App Engine Flexible Environment

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=flexible/sendgrid/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>


This sample demonstrates how to use [SendGrid](https://www.sendgrid.com) on
[Google App Engine flexible environment][aeflex-docs].

See the [sample application documentaion][sample-docs] for more detailed
instructions.

For more information about SendGrid, see their
[documentation](https://sendgrid.com/docs/User_Guide/index.html).

[aeflex-docs]: https://cloud.google.com/appengine/docs/flexible/
[sample-docs]: https://cloud.google.com/appengine/docs/flexible/java/sending-emails-with-sendgrid

## Setup

Before you can run or deploy the sample, you will need to do the following:

1. [Create a SendGrid Account](http://sendgrid.com/partner/google). As of
   September 2015, Google users start with 25,000 free emails per month.
1. Configure your SendGrid settings in the environment variables section in
   [`src/main/appengine/app.yaml`](src/main/appengine/app.yaml).

## Running locally

You can run the application locally and send emails from your local machine. You
will need to set environment variables before starting your application:

    $ export SENDGRID_API_KEY=[your-sendgrid-api-key]
    $ export SENDGRID_SENDER=[your-sendgrid-sender-email-address]
    $ mvn clean jetty:run
