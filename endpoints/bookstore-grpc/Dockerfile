# https://github.com/GoogleCloudPlatform/openjdk-runtime
FROM gcr.io/google_appengine/openjdk8

RUN echo 'deb http://httpredir.debian.org/debian jessie-backports main' > /etc/apt/sources.list.d/jessie-backports.list \
    && apt-get update \
    && apt-get install --no-install-recommends -y -q ca-certificates \
    && apt-get -y -q upgrade \
    && apt-get install --no-install-recommends -y openjdk-8-jre-headless \
    && rm -rf /var/lib/apt/lists/*

ADD ./server/build/libs/server.jar /bookstore/server.jar

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "/bookstore/server.jar"]
