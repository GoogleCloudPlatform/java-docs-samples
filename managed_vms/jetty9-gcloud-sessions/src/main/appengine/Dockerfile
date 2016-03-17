FROM gcr.io/google_appengine/jetty9
ADD jetty9-gcloud-session-1.0-SNAPSHOT.war $JETTY_BASE/webapps/root.war
ADD jetty-base $JETTY_BASE/
WORKDIR $JETTY_BASE
RUN java -jar $JETTY_HOME/start.jar --approve-all-licenses --add-to-startd=gcloud-sessions \
 && chown -R jetty:jetty $JETTY_BASE
RUN ls -lR
