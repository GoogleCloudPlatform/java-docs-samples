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

//[START tpu_queued_resources_network]
import com.google.api.gax.retrying.RetrySettings;
import com.google.cloud.tpu.v2alpha1.CreateQueuedResourceRequest;
import com.google.cloud.tpu.v2alpha1.NetworkConfig;
import com.google.cloud.tpu.v2alpha1.Node;
import com.google.cloud.tpu.v2alpha1.QueuedResource;
import com.google.cloud.tpu.v2alpha1.TpuClient;
import com.google.cloud.tpu.v2alpha1.TpuSettings;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.threeten.bp.Duration;

public class CreateQueuedResourceWithNetwork {
  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project you want to create a node.
    String projectId = "YOUR_PROJECT_ID";
    // The zone in which to create the TPU.
    // For more information about supported TPU types for specific zones,
    // see https://cloud.google.com/tpu/docs/regions-zones
    String zone = "europe-west4-a";
    // The name for your TPU.
    String nodeName = "YOUR_TPU_NAME";
    // The accelerator type that specifies the version and size of the Cloud TPU you want to create.
    // For more information about supported accelerator types for each TPU version,
    // see https://cloud.google.com/tpu/docs/system-architecture-tpu-vm#versions.
    String tpuType = "v2-8";
    // Software version that specifies the version of the TPU runtime to install.
    // For more information see https://cloud.google.com/tpu/docs/runtimes
    String tpuSoftwareVersion = "tpu-vm-tf-2.14.1";
    // The name for your Queued Resource.
    String queuedResourceId = "QUEUED_RESOURCE_ID";
    // The name of the network you want the node to connect to.
    // The network should be assigned to your project.
    String networkName = "YOUR_COMPUTE_TPU_NETWORK";

    createQueuedResourceWithNetwork(projectId, zone, queuedResourceId, nodeName,
        tpuType, tpuSoftwareVersion, networkName);
  }

  // Creates a Queued Resource with network configuration.
  public static QueuedResource createQueuedResourceWithNetwork(
      String projectId, String zone, String queuedResourceId, String nodeName,
      String tpuType, String tpuSoftwareVersion, String networkName)
      throws IOException, ExecutionException, InterruptedException {
    // With these settings the client library handles the Operation's polling mechanism
    // and prevent CancellationException error
    TpuSettings.Builder clientSettings =
        TpuSettings.newBuilder();
    clientSettings
        .createQueuedResourceSettings()
        .setRetrySettings(
            RetrySettings.newBuilder()
                .setInitialRetryDelay(Duration.ofMillis(5000L))
                .setRetryDelayMultiplier(2.0)
                .setInitialRpcTimeout(Duration.ZERO)
                .setRpcTimeoutMultiplier(1.0)
                .setMaxRetryDelay(Duration.ofMillis(45000L))
                .setTotalTimeout(Duration.ofHours(24L))
                .build());
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (TpuClient tpuClient = TpuClient.create(clientSettings.build())) {
      String parent = String.format("projects/%s/locations/%s", projectId, zone);
      String region = zone.substring(0, zone.length() - 2);

      // Specify the network and subnetwork that you want to connect your TPU to.
      NetworkConfig networkConfig =
          NetworkConfig.newBuilder()
              .setEnableExternalIps(true)
              .setNetwork(String.format("projects/%s/global/networks/%s", projectId, networkName))
              .setSubnetwork(
                  String.format(
                      "projects/%s/regions/%s/subnetworks/%s", projectId, region, networkName))
              .build();

      // Create a node
      Node node =
          Node.newBuilder()
              .setName(nodeName)
              .setAcceleratorType(tpuType)
              .setRuntimeVersion(tpuSoftwareVersion)
              .setNetworkConfig(networkConfig)
              .setQueuedResource(
                  String.format(
                      "projects/%s/locations/%s/queuedResources/%s",
                      projectId, zone, queuedResourceId))
              .build();

      // Create queued resource
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
              .setQueuedResource(queuedResource)
              .setQueuedResourceId(queuedResourceId)
              .build();
      // You can wait until TPU Node is READY,
      // and check its status using getTpuVm() from "tpu_vm_get" sample.

      return tpuClient.createQueuedResourceAsync(request).get();
    }
  }
}
//[END tpu_queued_resources_network]
