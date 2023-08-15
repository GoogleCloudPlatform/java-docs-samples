/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package cloudrun.snippets.jobs;

// [START cloudrun_list_jobs]
import com.google.cloud.run.v2.LocationName;
import com.google.cloud.run.v2.Job;
import com.google.cloud.run.v2.JobsClient;
import com.google.protobuf.Timestamp;
import java.io.IOException;
import java.time.Instant;

public class ListJobs {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String location = "us-central1";
    listJobs(projectId, location);
  }

  public static void listJobs(String projectId, String location) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (JobsClient jobsClient = JobsClient.create()) {
      // Set the location and project to list resources on.
      LocationName parent = LocationName.of(projectId, location);
      // Send request and iterate through Job objects
      for (Job job : jobsClient.listJobs(parent).iterateAll()) {
        // Print example usage of the Job object
        System.out.println("Job: " + job.getName());
        Timestamp ts = job.getCreateTime();
        Instant instant = Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
        System.out.println("Created at: " + instant.toString());
      }
    }
  }
}
// [END cloudrun_list_jobs]
