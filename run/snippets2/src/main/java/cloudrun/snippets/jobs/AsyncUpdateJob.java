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

// [START cloudrun_Jobs_UpdateJob_async]
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import com.google.api.core.ApiFuture;
import com.google.cloud.run.v2.Job;
import com.google.cloud.run.v2.JobsClient;
import com.google.cloud.run.v2.UpdateJobRequest;
import com.google.longrunning.Operation;

public class AsyncUpdateJob {

  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "PROJECT";
    String location = "us-central1";
    asyncUpdateJob(projectId, location);
  }

  public static void asyncUpdateJob(String projectId, String location) throws IOException, InterruptedException, ExecutionException {

    try (JobsClient jobsClient = JobsClient.create()) {
      UpdateJobRequest request =
          UpdateJobRequest.newBuilder()
              .setJob(Job.newBuilder().build())
              .setValidateOnly(true)
              .setAllowMissing(true)
              .build();
      ApiFuture<Operation> future = jobsClient.updateJobCallable().futureCall(request);
      // Do something.
      Operation response = future.get();
    }
  }
}
// [END cloudrun_Jobs_UpdateJob_async]
