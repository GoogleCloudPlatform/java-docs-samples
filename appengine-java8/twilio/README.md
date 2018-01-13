# Java Twilio Voice and SMS Sample for Google App Engine Standard Environment

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java8/twilio/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample demonstrates how to use [Twilio](https://www.twilio.com) on [Google
App Engine standard environment][ae-docs].

See the [sample application documentaion][sample-docs] for more detailed
instructions.

For more information about Twilio, see their [Java quickstart
tutorials](https://www.twilio.com/docs/quickstart/java).

[ae-docs]: https://cloud.google.com/appengine/docs/java/
[sample-docs]: https://cloud.google.com/appengine/docs/java/sms/twilio


## Setup

Before you can run or deploy the sample, you will need to do the following:

1. [Create a Twilio Account](http://ahoy.twilio.com/googlecloudplatform). Google
   App Engine customers receive a complimentary credit for SMS messages and
   inbound messages.
1. Create a number on twilio, and configure the voice request URL to be
   ``https://your-app-id.appspot.com/call/receive`` and the SMS request URL to
   be ``https://your-app-id.appspot.com/sms/receive``.
1. Configure your Twilio settings in the environment variables section in
   [`src/main/webapp/WEB-INF/appengine-web.xml`](src/main/webapp/WEB-INF/appengine-web.xml).

## Running locally

You can run the application locally to test the callbacks and SMS sending. You
will need to set environment variables before starting your application:

    $ export TWILIO_ACCOUNT_SID=[your-twilio-accoun-sid]
    $ export TWILIO_AUTH_TOKEN=[your-twilio-auth-token]
    $ export TWILIO_NUMBER=[your-twilio-number]
    $ mvn clean jetty:run
