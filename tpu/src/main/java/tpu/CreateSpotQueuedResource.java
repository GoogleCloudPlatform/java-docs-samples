"v5litepod-4";"v5litepod-4";/*
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

// [START tpu_queued_resources_create_spot]
import com.google.cloud.tpu.v2alpha1.CreateQueuedResourceRequest;
import com.google.cloud.tpu.v2alpha1.Node;
import com.google.cloud.tpu.v2alpha1.QueuedResource;
import com.google.cloud.tpu.v2alpha1.SchedulingConfig;
import com.google.cloud.tpu.v2alpha1.TpuClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class CreateSpotQueuedResource {
  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project you want to create a node.
    String projectId = "YOUR_PROJECT_ID";
    // The zone in which to create the TPU.
    // For more information about supported TPU types for specific zones,
    // see https://cloud.google.com/tpu/docs/regions-zones
    String zone = "us-central1-a";
    // The name for your TPU.
    String nodeName = "YOUR_TPU_NAME";
    // The accelerator type that specifies the version and size of the Cloud TPU you want to create.
    // For more information about supported accelerator types for each TPU version,
    // see https://cloud.google.com/tpu/docs/system-architecture-tpu-vm#versions.
    String tpuType = "v2-8";
    // Software version that specifies the version of the TPU runtime to install.
    // For more information see https://cloud.google.com/tpu/docs/runtimes
    String tpuSoftwareVersion = "v2-tpuv5-litepod";
    // The name for your Queued Resource.
    String queuedResourceId = "QUEUED_RESOURCE_ID";

    createQueuedResource(
        projectId, zone, queuedResourceId, nodeName, tpuType, tpuSoftwareVersion);
  }

  // Creates a Queued Resource with --preemptible flag.
  public static QueuedResource createQueuedResource(
      String projectId, String zone, String queuedResourceId,
      String nodeName, String tpuType, String tpuSoftwareVersion)
      throws IOException, ExecutionException, InterruptedException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (TpuClient tpuClient = TpuClient.create()) {
      String parent = String.format("projects/%s/locations/%s", projectId, zone);
      String resourceName = String.format("projects/%s/locations/%s/queuedResources/%s",
              projectId, zone, queuedResourceId);
      SchedulingConfig schedulingConfig = SchedulingConfig.newBuilder()
          .setPreemptible(true)
          .build();

      Node node =
          Node.newBuilder()
              .setName(nodeName)
              .setAcceleratorType(tpuType)
              .setRuntimeVersion(tpuSoftwareVersion)
              .setSchedulingConfig(schedulingConfig)
              .setQueuedResource(resourceName)
              .build();

      QueuedResource queuedResource =
          QueuedResource.newBuilder()
              .setName(queuedResourceId)
              .setTpu(
                  QueuedResource.Tpu.newBuilder()
                      .addNodeSpec(
                          QueuedResource.Tpu.NodeSpec.newBuilder()
                              .setParent(parent)
                              .setNode(node)
                              .setNodeId(nodeName)
                              .build())
                      .build())
              .build();

      CreateQueuedResourceRequest request =
          CreateQueuedResourceRequest.newBuilder()
              .setParent(parent)
              .setQueuedResourceId(queuedResourceId)
              .setQueuedResource(queuedResource)
              .build();

      return tpuClient.createQueuedResourceAsync(request).get();
    }
  }
}
// [END tpu_queued_resources_create_spot]
