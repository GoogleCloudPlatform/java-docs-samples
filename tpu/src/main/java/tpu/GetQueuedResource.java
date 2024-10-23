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

//[START tpu_queued_resources_get]

import com.google.cloud.tpu.v2alpha1.GetQueuedResourceRequest;
import com.google.cloud.tpu.v2alpha1.QueuedResource;
import com.google.cloud.tpu.v2alpha1.TpuClient;
import java.io.IOException;

public class GetQueuedResource {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project.
    String projectId = "YOUR_PROJECT_ID";
    // The zone in which the TPU was created.
    String zone = "europe-west4-a";
    // The name for your Queued Resource.
    String queuedResourceId = "QUEUED_RESOURCE_ID";

    getQueuedResource(projectId, zone, queuedResourceId);
  }

  // Get a Queued Resource.
  public static QueuedResource getQueuedResource(
      String projectId, String zone, String queuedResourceId) throws IOException {
    String name = String.format("projects/%s/locations/%s/queuedResources/%s",
              projectId, zone, queuedResourceId);
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (TpuClient tpuClient = TpuClient.create()) {
      GetQueuedResourceRequest request =
          GetQueuedResourceRequest.newBuilder().setName(name).build();

      return tpuClient.getQueuedResource(request);
    }
  }
}
//[END tpu_queued_resources_get]