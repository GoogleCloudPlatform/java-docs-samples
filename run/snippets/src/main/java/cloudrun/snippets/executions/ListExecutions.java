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

package cloudrun.snippets.executions;

import java.io.IOException;
import java.util.List;
import com.google.cloud.run.v2.ListExecutionsRequest;
import com.google.cloud.run.v2.ListExecutionsResponse;
import com.google.cloud.run.v2.Execution;
import com.google.cloud.run.v2.ExecutionsClient;
import com.google.cloud.run.v2.ServiceName;

public class ListExecutions {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String location = "us-central1";
    String jobId = "my-job-id";
    listExecutions(projectId, location, jobId);
  }

  public static List<Execution> listExecutions(String projectId, String location, String jobId)
      throws IOException {
    try (ExecutionsClient executionsClient = ExecutionsClient.create()) {
      ListExecutionsRequest request = ListExecutionsRequest.newBuilder()
          .setParent(ServiceName.of(projectId, location, jobId).toString())
          .build();
      ListExecutionsResponse response = executionsClient.listExecutionsCallable().call(request);
      // Do something.
      for (Execution execution : response.getExecutionsList()) {
        System.out.println(execution.getName());
      }
      return response.getExecutionsList();
    }
  }
}
