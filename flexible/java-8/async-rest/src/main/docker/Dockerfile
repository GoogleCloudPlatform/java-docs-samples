FROM gcr.io/google_appengine/jetty9

ADD async-rest-1.0.0-SNAPSHOT.war $JETTY_BASE/webapps/root.war
ADD jetty-logging.properties $JETTY_BASE/resources/jetty-logging.properties
RUN chown jetty:jetty $JETTY_BASE/webapps/root.war $JETTY_BASE/resources/jetty-logging.properties
WORKDIR $JETTY_BASE
#RUN java -jar $JETTY_HOME/start.jar --approve-all-licenses --add-to-startd=jmx,stats,hawtio

