# Java SendGrid Email Sample for Google App Engine Standard Environment

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java8/sendgrid/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

This sample demonstrates how to use [SendGrid](http://sendgrid.com/partner/google) on
[Google App Engine standard environment][ae-docs].

See the [sample application documentaion][sample-docs] for more detailed
instructions.

For more information about SendGrid, see their
[documentation](https://sendgrid.com/docs/for-developers/).

[ae-docs]: https://cloud.google.com/appengine/docs/standard/java/
[sample-docs]: https://cloud.google.com/appengine/docs/java/mail/sendgrid

## Setup

Before you can run or deploy the sample, you will need to do the following:

1. [Sign up with SendGrid via the GCP Console](https://console.cloud.google.com/launcher/details/sendgrid-app/sendgrid-email)
    and as a Google Cloud Platform developer, you can start with 12,000 free
    emails per month.
1. Create an API key in SendGrid and configure your SendGrid settings in the
    environment variables section in [`appengine-web.xml`](src/main/webapp/WEB-INF/appengine-web.xml).

    ```XML
    <env-variables>
      <env-var name="SENDGRID_API_KEY" value="YOUR-SENDGRID-API-KEY" />
      <env-var name="SENDGRID_SENDER" value="YOUR-SENDGRID-SENDER" />
    </env-variables>
    ```

## Running locally

You can run the application locally and send emails from your local machine. You
will need to set environment variables before starting your application:

```shell
export SENDGRID_API_KEY=[your-sendgrid-api-key]
export SENDGRID_SENDER=[your-sendgrid-sender-email-address]
mvn clean jetty:run
```

To send an email, visit `localhost:8080/send/email?to=[EMAIL-ADDRESS]`
in a web browser.

## Deploy

Deploy your application to App Engine standard with the following command:

  ```shell
  mvn appengine:deploy
  ```

To send an email, visit `https://[YOUR-PROJECT-ID].appspot.com/send/email?to=[EMAIL-ADDRESS]`
in a web browser.
