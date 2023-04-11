#  Copyright 2023 Google Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

FROM gcr.io/google_appengine/jetty9

ADD async-rest-1.0.0-SNAPSHOT.war $JETTY_BASE/webapps/root.war
ADD jetty-logging.properties $JETTY_BASE/resources/jetty-logging.properties
RUN chown jetty:jetty $JETTY_BASE/webapps/root.war $JETTY_BASE/resources/jetty-logging.properties
WORKDIR $JETTY_BASE
#RUN java -jar $JETTY_HOME/start.jar --approve-all-licenses --add-to-startd=jmx,stats,hawtio

