/*
 * Copyright 2017 Google Inc.
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

package com.example.flexible.errorreporting;

import com.google.cloud.ServiceOptions;
import com.google.cloud.errorreporting.v1beta1.ReportErrorsServiceClient;
import com.google.devtools.clouderrorreporting.v1beta1.ErrorContext;
import com.google.devtools.clouderrorreporting.v1beta1.ProjectName;
import com.google.devtools.clouderrorreporting.v1beta1.ReportedErrorEvent;

import com.google.devtools.clouderrorreporting.v1beta1.SourceLocation;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START flex_error_reporting]
// [START error_reporting_setup_java_appengine_flex]
@WebServlet(name = "Error reporting", value = "/error")
public class ErrorReportingExample extends HttpServlet {

  private Logger logger = Logger.getLogger(ErrorReportingExample.class.getName());

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {

    // errors logged to stderr / Cloud logging with exceptions are automatically reported.
    logger.log(Level.SEVERE, "exception using log framework", new IllegalArgumentException());

    // use the error-reporting client library only if you require logging custom error events.
    logCustomErrorEvent();

    // runtime exceptions are also automatically reported.
    throw new RuntimeException("this is a runtime exception");
  }

  private void logCustomErrorEvent() {
    try (ReportErrorsServiceClient reportErrorsServiceClient = ReportErrorsServiceClient.create()) {
      // Custom error events require an error reporting location as well.
      ErrorContext errorContext = ErrorContext.newBuilder()
          .setReportLocation(SourceLocation.newBuilder()
              .setFilePath("Test.java")
              .setLineNumber(10)
              .setFunctionName("myMethod")
              .build())
          .build();
      //Report a custom error event
      ReportedErrorEvent customErrorEvent = ReportedErrorEvent.getDefaultInstance()
          .toBuilder()
          .setMessage("custom error event")
          .setContext(errorContext)
          .build();

      // default project id
      ProjectName projectName = ProjectName.of(ServiceOptions.getDefaultProjectId());
      reportErrorsServiceClient.reportErrorEvent(projectName, customErrorEvent);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Exception encountered logging custom event", e);
    }
  }
}
// [END error_reporting_setup_java_appengine_flex]
// [END flex_error_reporting]
