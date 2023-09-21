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

package com.example.errorreporting;

// [START error_reporting_quickstart]
// [START error_reporting_setup_java]
import com.google.cloud.ServiceOptions;
import com.google.devtools.clouderrorreporting.v1beta1.ProjectName;
import com.google.devtools.clouderrorreporting.v1beta1.ReportErrorsServiceClient;
import com.google.devtools.clouderrorreporting.v1beta1.ReportedErrorEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Snippet demonstrates using the Error Reporting API to report an exception.
 * <p>
 * When the workload runs on App Engine, GKE, Cloud Functions or another managed environment,
 * printing the exception's stack trace to stderr will automatically report the error
 * to Error Reporting.
 */
public class QuickStart {

  static String projectId;

  public static void main(String[] args) throws Exception {
    // Set your Google Cloud Platform project ID via environment or explicitly
    projectId = ServiceOptions.getDefaultProjectId();
    if (args.length > 0) {
      projectId = args[0];
    } else {
      String value = System.getenv("GOOGLE_CLOUD_PROJECT");
      if (value != null && value.isEmpty()) {
        projectId = value;
      }
    }

    try {
      throw new Exception("Something went wrong");
    } catch (Exception ex) {
      reportError(ex);
    }
  }

  /**
   * Sends formatted error report to Google Cloud including the error context.
   *
   * @param ex Exception containing the error and the context.
   * @throws IOException if fails to communicate with Google Cloud
   */
  private static void reportError(Exception ex) throws IOException {
    try (ReportErrorsServiceClient serviceClient = ReportErrorsServiceClient.create()) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      ex.printStackTrace(pw);

      ReportedErrorEvent errorEvent = ReportedErrorEvent.getDefaultInstance()
          .toBuilder()
          .setMessage(sw.toString())
          .build();
      // If you need to report an error asynchronously, use reportErrorEventCallable()
      // method
      serviceClient.reportErrorEvent(ProjectName.of(projectId), errorEvent);
    }
  }
}
// [END error_reporting_setup_java]
// [END error_reporting_quickstart]
