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

//[START tpu_vm_create_startup_script]

import com.google.cloud.tpu.v2.CreateNodeRequest;
import com.google.cloud.tpu.v2.Node;
import com.google.cloud.tpu.v2.TpuClient;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class CreateTpuVmWithStartupScript {
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
    String nodeName = "YOUR_TPY_NAME";
    // The accelerator type that specifies the version and size of the Cloud TPU you want to create.
    // For more information about supported accelerator types for each TPU version,
    // see https://cloud.google.com/tpu/docs/system-architecture-tpu-vm#versions.
    String acceleratorType = "v2-8";
    // Software version that specifies the version of the TPU runtime to install.
    // For more information, see https://cloud.google.com/tpu/docs/runtimes
    String tpuSoftwareVersion = "tpu-vm-tf-2.14.1";

    createTpuVmWithStartupScript(projectId, zone, nodeName, acceleratorType, tpuSoftwareVersion);
  }

  // Creates a TPU VM with the specified name, zone, accelerator type, and version.
  public static void createTpuVmWithStartupScript(String projectId, String zone,
      String nodeName, String acceleratorType, String tpuSoftwareVersion)
      throws IOException, ExecutionException, InterruptedException {
    try (TpuClient tpuClient = TpuClient.create()) {
      String parent = String.format("projects/%s/locations/%s", projectId, zone);

      // Create metadata map
      Map<String, String> metadata = new HashMap<>();
      metadata.put("startup-script", "your-script-here"); // Script content here

      Node tpuVm =
          Node.newBuilder()
              .setName(nodeName)
              .setAcceleratorType(acceleratorType)
              .setRuntimeVersion(tpuSoftwareVersion)
              .putAllLabels(metadata)
              .build();

      CreateNodeRequest request =
          CreateNodeRequest.newBuilder()
              .setParent(parent)
              .setNodeId(nodeName)
              .setNode(tpuVm)
              .build();

      Node response = tpuClient.createNodeAsync(request).get();

      System.out.printf("TPU VM created: %s\n", response.getName());
    }
  }
}
//[END tpu_vm_create_startup_script]
