# https://github.com/GoogleCloudPlatform/openjdk-runtime
FROM gcr.io/google_appengine/openjdk8

RUN apt-get update \
    && apt-get -y -q upgrade \
    && rm -rf /var/lib/apt/lists/*

ADD ./server/build/libs/server.jar /hello/server.jar

ENTRYPOINT ["java", "-jar", "/hello/server.jar"]
