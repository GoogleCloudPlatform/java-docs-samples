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

package cloudrun.snippets.revisions;

// [START cloudrun_delete_revision]
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import com.google.cloud.run.v2.Revision;
import com.google.cloud.run.v2.RevisionName;
import com.google.cloud.run.v2.RevisionsClient;

public class DeleteRevision {

  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
    String projectId = "your-project-id";
    String location = "us-central1";
    String serviceId = "my-service-id";
    String revisionId = "my-service-id-revision";
    deleteRevision(projectId, location, serviceId, revisionId);
  }

  public static void deleteRevision(String projectId, String location, String serviceId, String revisionId) throws IOException, InterruptedException, ExecutionException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (RevisionsClient revisionsClient = RevisionsClient.create()) {
      RevisionName name = RevisionName.of(projectId, location, serviceId, revisionId);
      Revision response = revisionsClient.deleteRevisionAsync(name).get();
      System.out.println("Deleted revision: " + response.getName() + ", for service, " + response.getService());
    }
  }
}
// [END cloudrun_delete_revision]
