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

// [START cloudrun_Executions_ListExecutions_String_sync]
import java.io.IOException;
import com.google.cloud.run.v2.Execution;
import com.google.cloud.run.v2.ExecutionsClient;
import com.google.cloud.run.v2.JobName;

public class SyncListExecutionsString {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "PROJECT";
    String location = "us-central1";
    syncListExecutionsString(projectId, location);
  }

  public static void syncListExecutionsString(String projectId, String location) throws IOException {

    try (ExecutionsClient executionsClient = ExecutionsClient.create()) {
      String parent = JobName.of(projectId, location, "[JOB]").toString();
      for (Execution element : executionsClient.listExecutions(parent).iterateAll()) {
        // doThingsWith(element);
      }
    }
  }
}
// [END cloudrun_Executions_ListExecutions_String_sync]
