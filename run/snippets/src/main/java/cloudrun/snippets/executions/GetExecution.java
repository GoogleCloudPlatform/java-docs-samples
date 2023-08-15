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

package cloudrun.snippets.executions;

import java.time.Instant;
// [START run_v2_generated_Executions_GetExecution_Executionname_sync]
import com.google.cloud.run.v2.Execution;
import com.google.cloud.run.v2.ExecutionName;
import com.google.cloud.run.v2.ExecutionsClient;
import com.google.protobuf.Timestamp;

public class GetExecution {

  public static void main(String[] args) throws Exception {
    String projectId = "your-project-id";
    String location = "us-central1";
    String jobId = "my-job-id";
    String executionId = "my-job-id-execution";
    getExecution(projectId, location, jobId, executionId);
  }

  public static void getExecution(String projectId, String location, String jobId, String executionId) throws Exception {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ExecutionsClient executionsClient = ExecutionsClient.create()) {
      ExecutionName name = ExecutionName.of(projectId, location, jobId, executionId);
      Execution response = executionsClient.getExecution(name);
      System.out.println("Execution: " + response.getName());

      Timestamp start = response.getStartTime();
      Instant startTime = Instant.ofEpochSecond(start.getSeconds(), start.getNanos());
      Timestamp completed = response.getCompletionTime();
      long elapsedTime = completed.getSeconds() - start.getSeconds();
      System.out.println("Started: " + startTime);
      System.out.println("Elapsed time: " + elapsedTime);
      System.out.println("Log UIR: " + response.getLogUri());
    }
  }
}
// [END cloudrun_get_execution]
