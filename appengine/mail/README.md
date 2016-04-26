# JavaMail API Email Sample for Google App Engine Standard Environment

This sample demonstrates how to use [JavaMail][javamail-api] on [Google App Engine
standard environment][ae-docs].

See the [sample application documentaion][sample-docs] for more detailed
instructions.

[ae-docs]: https://cloud.google.com/appengine/docs/java/
[javamail-api]: http://javamail.java.net/
[sample-docs]: https://cloud.google.com/appengine/docs/java/mail/

## Setup
1. Update the `<application>` tag in `src/main/webapp/WEB-INF/appengine-web.xml`
   with your project name.
1. Update the `<version>` tag in `src/main/webapp/WEB-INF/appengine-web.xml`
   with your version name.

## Running locally
    $ mvn appengine:devserver

## Deploying
    $ mvn appengine:update
