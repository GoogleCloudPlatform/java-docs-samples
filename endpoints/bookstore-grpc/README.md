# Google Cloud Endpoints Bookstore App in Java

## Prerequisites

* [Java 8](http://openjdk.java.net/install/)
* [Docker](https://www.docker.com/products/docker)

## Building and Running the Server

The Java Bookstore gRPC example is built using Gradle:

    ./gradlew build

To run the Java server and client locally:

    # Start the server (listens on port 8000 by default)
    java -jar ./server/build/libs/server.jar

    # Run the client (connects to localhost:8000 by default)
    java -jar ./client/build/libs/client.jar
