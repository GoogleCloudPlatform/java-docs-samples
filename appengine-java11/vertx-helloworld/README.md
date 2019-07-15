# Vert.x Application on Google App Engine Standard with Java 11

This sample shows how to deploy a [Vert.x](https://vertx.io/)
application to Google App Engine stadndard.

## Setup

See [Prerequisites](../README.md#Prerequisites).

## Deploying

```bash
 mvn clean package appengine:deploy -Dapp.deploy.projectId=<your-project-id>
```

## See the application page
Navigate to `https://<your-project-id>.appspot.com`.

## The application

The application is written to use Eclipse Vert.x to demonstrate the use of [Vert.x Web](https://vertx.io/docs/vertx-web/java/) as web server
and [Vert.x Web client](https://vertx.io/docs/vertx-web-client/java/).

Vert.x is a fully non-blocking toolkit and some parts of the application use callbacks.

The [main](src/main/java/com/google/appengine/vertxhello/Main.java) class creates the Vert.x instance deploys the `Server` class:

```
Vertx vertx = Vertx.vertx();
vertx.deployVerticle(new Server());
```

## The application

When the [application](src/main/java/com/google/appengine/vertxhello/Application.java) starts

- it creates a Vert.x Web client for querying the Google metadata API for the project ID displayed in the response
- it creates a Vert.x Web router when it starts and initializes it to serve all the routes with the `handleDefault` method
- it starts an HTTP server on the port `8080`

```
webClient = WebClient.create(vertx);

Router router = Router.router(vertx);

router.route().handler(this::handleDefault);

vertx.createHttpServer()
    .requestHandler(router)
    .listen(8080, ar -> startFuture.handle(ar.mapEmpty()));
```

HTTP requests are served by the `handleDefault` method. This method uses the `WebClient` to query the Google metadata API
for the project ID and returns an _Hello World_ string that contains the project ID name.
