# Mailjet sample for Google App Engine

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java8/mailjet/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>


This sample demonstrates how to use [Mailjet](https://www.mailjet.com/) on Google Managed VMs to
send emails from a verified sender you own.

## Setup
1. Before using, ensure the address you plan to send from has been verified in Mailjet.

## Running locally
    $ export MAILJET_API_KEY=[your mailjet api key]
    $ export MAILJET_SECRET_KEY=[your mailjet secret key]
    $ mvn clean appengine:devserver

## Deploying
1. Edit the environment variables in the appengine-web.xml with the appropriate Mailjet values.
    $ mvn clean appengine:update
