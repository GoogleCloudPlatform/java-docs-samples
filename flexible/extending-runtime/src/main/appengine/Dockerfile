FROM gcr.io/google_appengine/jetty9

RUN apt-get update && apt-get install -y fortunes
ADD extendingruntime-1.0-SNAPSHOT.war $JETTY_BASE/webapps/root.war
