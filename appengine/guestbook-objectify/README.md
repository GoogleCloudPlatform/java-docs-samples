# appengine/guestbook-objectify

An App Engine guestbook using Java, Maven, and Objectify.

Data access using [Objectify](https://github.com/objectify/objectify)

Please ask questions on [Stackoverflow](http://stackoverflow.com/questions/tagged/google-app-engine)

## Running Locally

How do I, as a developer, start working on the project?

1. `mvn clean appengine:devserver`

## Deploying

1. `mvn clean appengine:update -Dappengine.appId=PROJECT -Dappengine.version=VERSION`
