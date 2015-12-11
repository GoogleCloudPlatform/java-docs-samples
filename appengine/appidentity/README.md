# App Identity sample for Google App Engine
This sample demonstrates how to use the App Identity APIs on Google App Engine

## Running locally
    $ mvn appengine:devserver

## Deploying
In the following command, replace YOUR-PROJECT-ID with your
[Google Cloud Project ID](https://developers.google.com/console/help/new/#projectnumber)
and YOUR-VERSION with a suitable version identifier.

    $ mvn appengine:update -Dappengine.appId=YOUR-PROJECT-ID -Dappengine.version=YOUR-VERSION

## Setup
To save your project settings so that you don't need to enter the
`-Dappengine.appId=YOUR-CLOUD-PROJECT-ID` or
`-Dappengine.version=YOUR-VERSION-NAME` parameters, you can make the following
changes:

1. Update the <application> tag in src/main/webapp/WEB-INF/appengine-web.xml with your project name
1. Update the <version> tag in src/main/webapp/WEB-INF/appengine-web.xml with your version name

You will now be able to run

    $ mvn appengine:update

without the need for any additional paramters.
