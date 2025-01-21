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

// [START tpu_queued_resources_time_bound]
import com.google.cloud.tpu.v2alpha1.CreateQueuedResourceRequest;
import com.google.cloud.tpu.v2alpha1.Node;
import com.google.cloud.tpu.v2alpha1.QueuedResource;
import com.google.cloud.tpu.v2alpha1.TpuClient;
import com.google.protobuf.Duration;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class CreateTimeBoundQueuedResource {

  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project you want to create a node.
    String projectId = "YOUR_PROJECT_ID";
    // The zone in which to create the TPU.
    // For more information about supported TPU types for specific zones,
    // see https://cloud.google.com/tpu/docs/regions-zones
    String zone = "us-central2-b";
    // The name of your node.
    String nodeId = "YOUR_NODE_ID";
    // The accelerator type that specifies the version and size of the Cloud TPU you want to create.
    // For more information about supported accelerator types for each TPU version,
    // see https://cloud.google.com/tpu/docs/system-architecture-tpu-vm#versions.
    String acceleratorType = "v2-8";
    // Software version that specifies the version of the TPU runtime to install.
    // For more information see https://cloud.google.com/tpu/docs/runtimes
    String runtimeVersion = "tpu-vm-tf-2.14.1";
    // The name of your Queued Resource.
    String queuedResourceId = "YOUR_QUEUED_RESOURCE_ID";

    createTimeBoundQueuedResource(projectId, nodeId,
        queuedResourceId, zone, acceleratorType, runtimeVersion);
  }

  // Creates a Queued Resource with time bound configuration.
  public static QueuedResource createTimeBoundQueuedResource(
      String projectId, String nodeId, String queuedResourceId,
      String zone, String acceleratorType, String runtimeVersion)
          throws IOException, ExecutionException, InterruptedException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (TpuClient tpuClient = TpuClient.create()) {
      String parent = String.format("projects/%s/locations/%s", projectId, zone);
      // Create a Duration object representing 6 hours.
      Duration validAfterDuration = Duration.newBuilder().setSeconds(6 * 3600).build();
      // You could also use timestamps like this:
      // Timestamp validAfterTime = Timestamps.parse("2024-10-14T09:00:00Z");

      Node node =
          Node.newBuilder()
              .setName(nodeId)
              .setAcceleratorType(acceleratorType)
              .setRuntimeVersion(runtimeVersion)
              .setQueuedResource(
                  String.format(
                      "projects/%s/locations/%s/queuedResources/%s",
                      projectId, zone, queuedResourceId))
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
                              .setNodeId(nodeId)
                              .build())
                      .build())
              .setQueueingPolicy(
                  QueuedResource.QueueingPolicy.newBuilder()
                      .setValidAfterDuration(validAfterDuration)
                      // .setValidAfterTime(validAfterTime)
                      .build())
              .build();

      CreateQueuedResourceRequest request =
          CreateQueuedResourceRequest.newBuilder()
              .setParent(parent)
              .setQueuedResource(queuedResource)
              .setQueuedResourceId(queuedResourceId)
              .build();

      return tpuClient.createQueuedResourceAsync(request).get();
    }
  }
}
// [END tpu_queued_resources_time_bound]