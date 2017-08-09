# Pusher sample for Google App Engine

This sample demonstrates how to use the [Pusher][pusher] on [Google App
Engine][ae-docs].
Pusher enables you to create public / private channels with presence information for real time messaging.
This application demonstrates presence channels in Pusher using chat rooms.
All users joining the chat room are authenticated.
All users currently in the chat room receive updates of users joining / leaving the room.
We will be using the [Java HTTP library](https://github.com/pusher/pusher-http-java) for publishing messages to the channel
and will be subscribing to channels using JS.

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
- Update Line 73 in [chat.jsp](src/webapp/WEB-INF/view/chat.jsp) with the `app_id` and cluster information.


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
