// Copyright 2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.batch;

// [START batch_job_logs]
import com.google.cloud.batch.v1.Job;
import com.google.cloud.logging.v2.LoggingClient;
import com.google.logging.v2.ListLogEntriesRequest;
import com.google.logging.v2.LogEntry;
import java.io.IOException;

public class ReadJobLogs {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project hosting the job.
    String projectId = "YOUR_PROJECT_ID";

    // The job which logs you want to print.
    Job job = Job.newBuilder().build();

    readJobLogs(projectId, job);
  }

  // Prints the log messages created by given job.
  public static void readJobLogs(String projectId, Job job) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `loggingClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (LoggingClient loggingClient = LoggingClient.create()) {

      ListLogEntriesRequest request = ListLogEntriesRequest.newBuilder()
          .addResourceNames(String.format("projects/%s", projectId))
          .setFilter(String.format("labels.job_uid=%s", job.getUid()))
          .build();

      for (LogEntry logEntry : loggingClient.listLogEntries(request).iterateAll()) {
        System.out.println(logEntry.getTextPayload());
      }
    }
  }
}
// [END batch_job_logs]