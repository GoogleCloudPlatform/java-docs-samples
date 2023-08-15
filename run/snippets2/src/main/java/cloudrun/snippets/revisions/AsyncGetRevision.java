/*
 * Copyright 2022 Google LLC
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

// [START cloudrun_revisionsclient_getrevision_async]
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import com.google.api.core.ApiFuture;
import com.google.cloud.run.v2.GetRevisionRequest;
import com.google.cloud.run.v2.Revision;
import com.google.cloud.run.v2.RevisionName;
import com.google.cloud.run.v2.RevisionsClient;

public class AsyncGetRevision {

  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "PROJECT";
    String location = "us-central1";
    asyncGetRevision(projectId, location);
  }

  public static void asyncGetRevision(String projectId, String location) throws IOException, InterruptedException, ExecutionException {

    try (RevisionsClient revisionsClient = RevisionsClient.create()) {
      GetRevisionRequest request =
          GetRevisionRequest.newBuilder()
              .setName(
                  RevisionName.of(projectId, location, "[SERVICE]", "[REVISION]").toString())
              .build();
      ApiFuture<Revision> future = revisionsClient.getRevisionCallable().futureCall(request);
      // Do something.
      Revision response = future.get();
    }
  }
}
// [END cloudrun_revisionsclient_getrevision_async]
