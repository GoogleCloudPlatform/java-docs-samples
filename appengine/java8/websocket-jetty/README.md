# App Engine Flexible Environment - Web Socket Example
This sample demonstrates how to use [Websockets](https://tools.ietf.org/html/rfc6455) on [Google App Engine Flexible Environment](https://cloud.google.com/appengine/docs/flexible/java/) using Java.
The sample uses the [native Jetty WebSocket Server API](http://www.eclipse.org/jetty/documentation/9.4.x/jetty-websocket-server-api.html) to create a server-side socket
and the [native Jetty WebSocket Client API](http://www.eclipse.org/jetty/documentation/9.4.x/jetty-websocket-client-api.html).

## Sample application workflow

1. The sample application creates a server socket using the endpoint  `/echo`.
1. The homepage (`/`) provides a form to submit a text message to the server socket. This creates a client-side socket
and sends the message to the server.
1. The server on receiving the message, echoes the message back to the client.
1. The message received by the client is stored  in an in-memory cache and is viewable on the homepage.

The sample also provides a Javascript [client](src/main/webapp/js_client.jsp)(`/js_client.jsp`) that you can use to test against the Websocket server.

## Setup

 - [Install](https://cloud.google.com/sdk/) and initialize GCloud SDK. This will
 ```
    gcloud init
 ```
-  If this is your first time creating an app engine application
  ```
    gcloud appengine create
  ```

## Local testing

Run using the [Jetty Maven plugin](http://www.eclipse.org/jetty/documentation/9.4.x/jetty-maven-plugin.html).
```
mvn jetty:run
```
You can then direct your browser to `http://localhost:8080/`

To test the Javascript client, access `http://localhost:8080/js_client.jsp`

## App Engine Flex Deployment

#### `app.yaml` Configuration

App Engine Flex deployment configuration is provided in [app.yaml](src/main/appengine/app.yaml).

Set the environment variable `JETTY_MODULES_ENABLE:websocket` to enable the Jetty websocket module on the Jetty server.

Manual scaling is set to a single instance as we are using an in-memory cache of messages for this sample application.

For more details on configuring your `app.yaml`, please refer to [this resource](https://cloud.google.com/appengine/docs/flexible/nodejs/configuring-your-app-with-app-yaml).

#### Deploy

The sample application is packaged as a war, and hence will be automatically run using the [Java 8/Jetty 9 with Servlet 3.1 Runtime](https://cloud.google.com/appengine/docs/flexible/java/dev-jetty9).

```
mvn appengine:deploy
```
You can then direct your browser to `https://YOUR_PROJECT_ID.appspot.com/`

To test the Javascript client, access `https://YOUR_PROJECT_ID.appspot.com/js_client.jsp`

Note: This application constructs a Web Socket URL using `getWebSocketAddress`
in the [SendServlet Class](src/main/java/com/example/flexible/websocket/jettynative/SendServlet.java)
. The application assumes the latest version of the service.
