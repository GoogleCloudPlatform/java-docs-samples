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

// [START cloudrun_revisionsclient_listrevisions_async]
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import com.google.api.core.ApiFuture;
import com.google.cloud.run.v2.ListRevisionsRequest;
import com.google.cloud.run.v2.Revision;
import com.google.cloud.run.v2.RevisionsClient;
import com.google.cloud.run.v2.ServiceName;
import com.google.cloud.run.v2.ServicesClient;

public class AsyncListRevisions {

  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "PROJECT";
    String location = "us-central1";
    asyncListRevisions(projectId, location);
  }

  public static void asyncListRevisions(String projectId, String location) throws IOException, InterruptedException, ExecutionException {

    try (RevisionsClient revisionsClient = RevisionsClient.create()) {
      ListRevisionsRequest request =
          ListRevisionsRequest.newBuilder()
              .setParent(ServiceName.of(projectId, location, "[SERVICE]").toString())
              .setPageSize(883849137)
              .setPageToken("pageToken873572522")
              .setShowDeleted(true)
              .build();
      ApiFuture<RevisionsClient.ListRevisionsPagedResponse> future = revisionsClient.listRevisionsPagedCallable().futureCall(request);
      // Do something.
      for (Revision element : future.get().iterateAll()) {
        // doThingsWith(element);
      }
    }
  }
}
// [END cloudrun_revisionsclient_listrevisions_async]
