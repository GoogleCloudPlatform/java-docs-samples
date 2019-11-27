# Java SendGrid Email Sample for Google App Engine Standard Environment

**Java 7 runtime support on App Engine standard was [deprecated](https://cloud.google.com/appengine/docs/deprecations/java7) on
December 13, 2017 and will be shut down entirely on January 16, 2019. It is replaced by the
[Java 8 Runtime Environment](https://cloud.google.com/appengine/docs/standard/java/runtime-java8).**

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine/sendgrid/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>


Samples for the Java 8 runtime can be found [here](/appengine-java8).

This sample demonstrates how to use [SendGrid](https://www.sendgrid.com) on
[Google App Engine standard environment][ae-docs].

See the [sample application documentaion][sample-docs] for more detailed
instructions.

For more information about SendGrid, see their
[documentation](https://sendgrid.com/docs/User_Guide/index.html).

[ae-docs]: https://cloud.google.com/appengine/docs/java/
[sample-docs]: https://cloud.google.com/appengine/docs/java/mail/sendgrid

## Setup and deploy

Before you can run or deploy the sample, you will need to do the following:

1. [Create a SendGrid Account](http://sendgrid.com/partner/google). As of
   September 2015, Google users start with 25,000 free emails per month.
1. Configure your SendGrid settings in the environment variables section in
   [`src/main/webapp/WEB-INF/appengine-web.xml`](src/main/webapp/WEB-INF/appengine-web.xml).
1. Visit /send/email?to=YOUR-EMAIL-ADDRESS

## Running locally

You can run the application locally and send emails from your local machine. You
will need to set environment variables before starting your application:

    $ export SENDGRID_API_KEY=[your-sendgrid-api-key]
    $ export SENDGRID_SENDER=[your-sendgrid-sender-email-address]
    $ mvn clean jetty:run
