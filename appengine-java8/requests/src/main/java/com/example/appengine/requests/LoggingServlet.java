/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.appengine.requests;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START gae_java8_logging_example]
// With @WebServlet annotation the webapp/WEB-INF/web.xml is no longer required.
@WebServlet(
    name = "RequestLogging",
    description = "Requests: Logging example",
    urlPatterns = "/requests/log"
)
public class LoggingServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(LoggingServlet.class.getName());

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    log.info("An informational message.");
    log.warning("A warning message.");
    log.severe("An error message.");
    // [START_EXCLUDE]
    resp.setContentType("text/plain");
    resp.getWriter().println("Check logs for results");
    // [END_EXCLUDE]
  }
}
// [END gae_java8_logging_example]
