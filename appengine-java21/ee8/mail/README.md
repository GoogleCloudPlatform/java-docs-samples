# JakartaMail API Email Sample for Google App Engine Standard Environment

This sample demonstrates how to use [JakartaMail][jakartamail-api] on [Google App Engine
standard environment][ae-docs].

See the [sample application documentaion][sample-docs] for more detailed
instructions.

[ae-docs]: https://cloud.google.com/appengine/docs/java/
[jakartamail-api]: https://jakartaee.github.io/mail-api/
[sample-docs]: https://cloud.google.com/appengine/docs/java/mail/

## Setup

    gcloud init

## Running locally
    $ mvn appengine:run

## Deploying
    $ mvn clean package appengine:deploy
