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

//[START tpu_vm_create_topology]
import com.google.api.gax.longrunning.OperationTimedPollAlgorithm;
import com.google.api.gax.retrying.RetrySettings;
import com.google.cloud.tpu.v2.AcceleratorConfig;
import com.google.cloud.tpu.v2.AcceleratorConfig.Type;
import com.google.cloud.tpu.v2.CreateNodeRequest;
import com.google.cloud.tpu.v2.Node;
import com.google.cloud.tpu.v2.TpuClient;
import com.google.cloud.tpu.v2.TpuSettings;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.threeten.bp.Duration;

public class CreateTpuWithTopologyFlag {

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
    // The version of the Cloud TPU you want to create.
    // Available options: TYPE_UNSPECIFIED = 0, V2 = 2, V3 = 4, V4 = 7
    Type tpuVersion = AcceleratorConfig.Type.V2;
    // Software version that specifies the version of the TPU runtime to install.
    // For more information, see https://cloud.google.com/tpu/docs/runtimes
    String tpuSoftwareVersion = "tpu-vm-tf-2.17.0-pod-pjrt";
    // The physical topology of your TPU slice.
    // For more information about topology for each TPU version,
    // see https://cloud.google.com/tpu/docs/system-architecture-tpu-vm#versions.
    String topology = "2x2";

    createTpuWithTopologyFlag(projectId, zone, nodeName, tpuVersion, tpuSoftwareVersion, topology);
  }

  // Creates a TPU VM with the specified name, zone, version and topology.
  public static Node createTpuWithTopologyFlag(String projectId, String zone, String nodeName,
       Type tpuVersion, String tpuSoftwareVersion, String topology)
      throws IOException, ExecutionException, InterruptedException {
    // With these settings the client library handles the Operation's polling mechanism
    // and prevent CancellationException error
    TpuSettings.Builder clientSettings =
        TpuSettings.newBuilder();
    clientSettings
        .createNodeOperationSettings()
        .setPollingAlgorithm(
            OperationTimedPollAlgorithm.create(
                RetrySettings.newBuilder()
                    .setInitialRetryDelay(Duration.ofMillis(5000L))
                    .setRetryDelayMultiplier(1.5)
                    .setMaxRetryDelay(Duration.ofMillis(45000L))
                    .setInitialRpcTimeout(Duration.ZERO)
                    .setRpcTimeoutMultiplier(1.0)
                    .setMaxRpcTimeout(Duration.ZERO)
                    .setTotalTimeout(Duration.ofHours(24L))
                    .build()));

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (TpuClient tpuClient = TpuClient.create(clientSettings.build())) {
      String parent = String.format("projects/%s/locations/%s", projectId, zone);

      Node tpuVm =
          Node.newBuilder()
              .setName(nodeName)
              .setAcceleratorConfig(Node.newBuilder()
                  .getAcceleratorConfigBuilder()
                  .setType(tpuVersion)
                  .setTopology(topology)
                  .build())
              .setRuntimeVersion(tpuSoftwareVersion)
              .build();

      CreateNodeRequest request =
          CreateNodeRequest.newBuilder()
              .setParent(parent)
              .setNodeId(nodeName)
              .setNode(tpuVm)
              .build();

      return tpuClient.createNodeAsync(request).get();
    }
  }
}
//[END tpu_vm_create_topology]