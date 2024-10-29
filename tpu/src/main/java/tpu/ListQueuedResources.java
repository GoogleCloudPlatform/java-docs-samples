/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tpu;

//[START tpu_queued_resources_list]
import com.google.cloud.tpu.v2alpha1.ListQueuedResourcesRequest;
import com.google.cloud.tpu.v2alpha1.TpuClient;
import java.io.IOException;

public class ListQueuedResources {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project.
    String projectId = "YOUR_PROJECT_ID";
    // The zone in which the TPU was created.
    String zone = "europe-west4-a";

    listQueuedResources(projectId, zone);
  }

  // List Queued Resources.
  public static TpuClient.ListQueuedResourcesPagedResponse listQueuedResources(
      String projectId, String zone) throws IOException {
    String parent = String.format("projects/%s/locations/%s", projectId, zone);
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (TpuClient tpuClient = TpuClient.create()) {
      ListQueuedResourcesRequest request =
          ListQueuedResourcesRequest.newBuilder().setParent(parent).build();

      return tpuClient.listQueuedResources(request);
    }
  }
}
//[END tpu_queued_resources_list]
