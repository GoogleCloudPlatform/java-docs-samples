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

// [START cloudrun_delete_execution]
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import com.google.cloud.run.v2.Execution;
import com.google.cloud.run.v2.ExecutionName;
import com.google.cloud.run.v2.ExecutionsClient;

public class DeleteExecution {

  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
    String projectId = "your-project-id";
    String location = "us-central1";
    String jobId = "my-job-id";
    String executionId = "my-job-id-execution";
    deleteExecution(projectId, location, jobId, executionId);
  }

  public static void deleteExecution(String projectId, String location, String jobId, String executionId) throws IOException, InterruptedException, ExecutionException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ExecutionsClient executionsClient = ExecutionsClient.create()) {
      ExecutionName name = ExecutionName.of(projectId, location, jobId, executionId);
      Execution response = executionsClient.deleteExecutionAsync(name).get();
      System.out.println("Deleted execution: " + response.getName() + ", for job, " + response.getJob());
    }
  }
}
// [END cloudrun_delete_execution]
