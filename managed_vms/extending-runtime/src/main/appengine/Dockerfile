FROM gcr.io/google_appengine/jetty9

RUN apt-get update && apt-get install -y fortunes
ADD managed-vms-extendingruntime-1.0-SNAPSHOT.war $JETTY_BASE/webapps/root.war
