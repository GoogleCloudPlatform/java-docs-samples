FROM gcr.io/google_appengine/openjdk
VOLUME /tmp
ADD spark-1.0-jar-with-dependencies.jar app.jar
CMD [ "java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
