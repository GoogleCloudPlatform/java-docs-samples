# Google App Engine Standard Environment Java Samples

This is a repository that contains Java code samples for [Google App Engine
standard environment][ae-docs].

[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Prerequisites

### Download Maven

These samples use the [Apache Maven][maven] build system. Before getting
started, be sure to [download][maven-download] and [install][maven-install] it.
When you use Maven as described here, it will automatically download the needed
client libraries.

[maven]: https://maven.apache.org
[maven-download]: https://maven.apache.org/download.cgi
[maven-install]: https://maven.apache.org/install.html

### Create a Project in the Google Cloud Platform Console

If you haven't already created a project, create one now. Projects enable you to
manage all Google Cloud Platform resources for your app, including deployment,
access control, billing, and services.

1. Open the [Cloud Platform Console][cloud-console].
1. In the drop-down menu at the top, select **Create a project**.
1. Give your project a name.
1. Make a note of the project ID, which might be different from the project
   name. The project ID is used in commands and in configurations.

[cloud-console]: https://console.cloud.google.com/


## Samples

### Hello World

This sample demonstrates how to deploy an application on Google App Engine.

- [Documentation][ae-docs]
- [Code](helloworld)

### Sending Email

#### Sending Email with Mailgun

This sample demonstrates how to send email using the [Mailgun API][mailgun-api].

- [Documentation][mailgun-sample-docs]
- [Code](mailgun)

[mailgun-api]: https://documentation.mailgun.com/
[mailgun-sample-docs]: https://cloud.google.com/appengine/docs/java/mail/mailgun

#### Sending Email with SendGrid

This sample demonstrates how to send email using the [SendGrid][sendgrid].

- [Documentation][sendgrid-sample-docs]
- [Code](sendgrid)

[sendgrid]: https://sendgrid.com/docs/User_Guide/index.html
[sendgrid-sample-docs]: https://cloud.google.com/appengine/docs/java/mail/sendgrid

### Sending SMS with Twilio

This sample demonstrates how to use [Twilio](https://www.twilio.com) on [Google
App Engine standard environment][ae-docs].

- [Documentation][twilio-sample-docs]
- [Code](twilio)

[twilio-sample-docs]: https://cloud.google.com/appengine/docs/java/sms/twilio

### App Identity

This sample demonstrates how to use the [App Identity API][appid] to discover
the application's ID and assert identity to Google and third-party APIs.

- [Documentation][appid]
- [Code](appidentity)

[appid]: https://cloud.google.com/appengine/docs/java/appidentity/

### Other Samples

- [Sample Applications][sample-apps]

[sample-apps]: https://cloud.google.com/appengine/docs/java/samples


## Contributing changes

See [CONTRIBUTING.md](../CONTRIBUTING.md).

## Licensing

See [LICENSE](../LICENSE).

