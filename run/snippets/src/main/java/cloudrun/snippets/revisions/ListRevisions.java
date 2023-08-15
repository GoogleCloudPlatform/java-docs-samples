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

package cloudrun.snippets.revisions;

// [START cloudrun_list_revisions]
import java.io.IOException;
import java.util.List;
import com.google.api.core.ApiFuture;
import com.google.cloud.run.v2.ListRevisionsRequest;
import com.google.cloud.run.v2.ListRevisionsResponse;
import com.google.cloud.run.v2.Revision;
import com.google.cloud.run.v2.RevisionsClient;
import com.google.cloud.run.v2.ServiceName;
import com.google.cloud.run.v2.RevisionsClient.ListRevisionsPagedResponse;

public class ListRevisions {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String location = "us-central1";
    String serviceId = "my-service-id";
    listRevisions(projectId, location, serviceId);
  }

  public static List<Revision> listRevisions(String projectId, String location, String serviceId)
      throws IOException {
    try (RevisionsClient revisionsClient = RevisionsClient.create()) {
      ListRevisionsRequest request = ListRevisionsRequest.newBuilder()
          .setParent(ServiceName.of(projectId, location, serviceId).toString())
          .build();
      ListRevisionsResponse response = revisionsClient.listRevisionsCallable().call(request);
      // Do something.
      for (Revision revision : response.getRevisionsList()) {
        System.out.println(revision.getName());
      }
      return response.getRevisionsList();
    }
  }

  // public static ListRevisionsPagedResponse listRevisions(String projectId, String location, String serviceId) throws IOException {
  //   // Initialize client that will be used to send requests. This client only needs to be created
  //   // once, and can be reused for multiple requests.
  //   try (RevisionsClient revisionsClient = RevisionsClient.create()) {
  //     ServiceName parent = ServiceName.of(projectId, location, serviceId);
  //     System.out.printf("Service, %s, has revisions:\n", parent);
  //     ListRevisionsPagedResponse revisions = revisionsClient.listRevisions(parent);
  //     for (Revision revision : revisions.iterateAll()) {
  //       System.out.println(revision.getName());
  //     }
  //     return revisions;
  //   }
  // }
}
// [END run_v2_generated_Revisions_ListRevisions_Servicename_sync]
