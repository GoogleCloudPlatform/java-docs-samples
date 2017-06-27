/**
 * Copyright 2017 Google Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.flexible.errorreporting;

import com.google.cloud.ServiceOptions;
import com.google.cloud.errorreporting.v1beta1.ReportErrorsServiceClient;
import com.google.devtools.clouderrorreporting.v1beta1.ProjectName;
import com.google.devtools.clouderrorreporting.v1beta1.ReportedErrorEvent;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START flex_error_reporting]
@WebServlet(name = "Error reporting", value = "/error")
public class ErrorReportingExample extends HttpServlet {

  private Logger logger = Logger.getLogger(ErrorReportingExample.class.getName());

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {

    // errors logged to stderr / Cloud logging with exceptions are automatically reported.
    logger.log(Level.SEVERE, "exception using log framework", new IllegalArgumentException());

    // use the error-reporting client library to log custom error events
    logCustomErrorEvent();

    // runtime exceptions are also automatically picked up by error reporting.
    throw new RuntimeException("this is a runtime exception");
  }

  private void logCustomErrorEvent() {
    try (ReportErrorsServiceClient reportErrorsServiceClient = ReportErrorsServiceClient.create()) {
      // custom exception logged using the API
      Exception e = new Exception("custom event reported using the API");
      // Events reported using the API must contain a stack trace message
      StringWriter stackTrace = new StringWriter();
      e.printStackTrace(new PrintWriter(stackTrace));
      ReportedErrorEvent errorEvent =
          ReportedErrorEvent.getDefaultInstance()
              .toBuilder()
              .setMessage(stackTrace.toString())
              .build();
      // default project id
      ProjectName projectName = ProjectName.create(ServiceOptions.getDefaultProjectId());
      reportErrorsServiceClient.reportErrorEvent(projectName, errorEvent);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Exception encountered logging custom event", e);
    }
  }
}
// [END flex_error_reporting]
