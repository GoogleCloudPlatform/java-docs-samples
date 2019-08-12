# Pusher sample for Google App Engine

**Java 7 runtime support on App Engine standard was [deprecated](https://cloud.google.com/appengine/docs/deprecations/java7) on
December 13, 2017 and will be shut down entirely on January 16, 2019. It is replaced by the
[Java 8 Runtime Environment](https://cloud.google.com/appengine/docs/standard/java/runtime-java8).**

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine/pusher-chat/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>


Samples for the Java 8 runtime can be found [here](/appengine-java8).

This sample demonstrates how to use the [Pusher][pusher] on [Google App Engine][ae-docs].
Pusher enables you to create public / private channels with presence information for real time messaging.
This application demonstrates presence channels in Pusher using chat rooms.
All users joining the chat room are authenticated using the `/authorize` endpoint.
All users currently in the chat room receive updates of users joining / leaving the room.
[Java HTTP library](https://github.com/pusher/pusher-http-java) is used for publishing messages to the channel
and the [JS Websocket library](https://github.com/pusher/pusher-js) is used for subscribing.

[pusher]: https://pusher.com
[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Setup

Install the [Google Cloud SDK](https://cloud.google.com/sdk/) and run:
```
   gcloud init
```
If this is your first time creating an App engine application:
```
   gcloud app create
```

#### Setup Pusher

- Create a [Pusher] application and note down the `app_id`, `app_key`, `app_secret` and the cluster.
- Update [appengine-web.xml](src/main/webapp/WEB-INF/appengine-web.xml) with these credentials.

## Running locally

```
   mvn clean appengine:run
```

Access [http://localhost:8080](http://localhost:8080) via the browser, login and join the chat room.
The chat window will contain a link you can use to join the room as a different user in another browser.
You should now be able to view both the users within the chat application window and send messages to one another.

## Deploying

- Deploy the application to the project
  ```
       mvn clean appengine:deploy

  ```
  Access `https://YOUR_PROJECT_ID.appspot.com`
