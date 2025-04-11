# App Engine Flex custom runtime

To run the application locally:

```yaml
mvn clean package
mvn jetty:run
```

To compile and build the image locally:

```sh
docker build -t my-java-app .
docker run -p 8080:8080 my-java-app
```

To build the image using cloud build, please replcace the values in the cloudbuild.yaml file:

```sh
gcloud builds submit .
```

Deloy it in App Engine using container image:

```sh
gcloud app deploy --image-url=asia-docker.pkg.dev/${_PROJECT}/${_REPOSITORY}/my-java-app:v1
```
