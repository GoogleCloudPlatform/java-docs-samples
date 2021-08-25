## Building the application
Run `mvn clean package` to generate a runnable JAR file.

## Running locally
To run the application locally, use the following command:

```
java -Djava.security.egd=file:/dev/./urandom -jar target/jobs-example-0.0.1.jar
```

## Containerizing with Buildpacks
To convert the application into a container for use with Cloud Run jobs, use the following [`pack`](https://buildpacks.io/docs/tools/pack/) command:

```
pack build <TODO>
```
