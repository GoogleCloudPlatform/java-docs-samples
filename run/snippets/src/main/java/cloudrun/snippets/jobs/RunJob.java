/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloudrun.snippets.jobs;

import com.google.cloud.run.v2.Execution;
// [START cloudrun_get_job]
import com.google.cloud.run.v2.Job;
import com.google.cloud.run.v2.JobName;
import com.google.cloud.run.v2.JobsClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class RunJob {

  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String location = "us-central1";
    String jobId = "my-job";
    runJob(projectId, location, jobId);
  }

  public static String runJob(
      String projectId, String location, String jobId) throws IOException, InterruptedException, ExecutionException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (JobsClient jobsClient = JobsClient.create()) {
      // Define the full name of the Job.
      JobName name = JobName.of(projectId, location, jobId);
      // Send the request
      Execution response = jobsClient.runJobAsync(name).get();
      // Print example usage of the Job object
      System.out.println("Started job execution: " + response.getName());
      return response.getName();
    }
  }
}
// [END cloudrun_get_job]
