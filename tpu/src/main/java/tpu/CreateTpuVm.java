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

//[START tpu_vm_create]

import com.google.cloud.tpu.v2.CreateNodeRequest;
import com.google.cloud.tpu.v2.Node;
import com.google.cloud.tpu.v2.TpuClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class CreateTpuVm {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "YOUR_PROJECT_ID";
    String zone = "europe-west4-a";
    String tpuVmName = "YOUR_TPU_NAME";
    String acceleratorType = "v2-8";
    String version = "tpu-vm-tf-2.14.1";

    createTpuVm(projectId, zone, tpuVmName, acceleratorType, version);
  }

  // Creates a TPU VM with the specified name, zone, accelerator type, and version.
  public static void createTpuVm(
      String projectId, String zone, String tpuVmName, String acceleratorType, String version)
      throws IOException, ExecutionException, InterruptedException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (TpuClient tpuClient = TpuClient.create()) {
      String parent = String.format("projects/%s/locations/%s", projectId, zone);

      Node tpuVm = Node.newBuilder()
              .setName(tpuVmName)
              .setAcceleratorType(acceleratorType)
              .setRuntimeVersion(version)
              .build();

      CreateNodeRequest request = CreateNodeRequest.newBuilder()
              .setParent(parent)
              .setNodeId(tpuVmName)
              .setNode(tpuVm)
              .build();

      Node response = tpuClient.createNodeAsync(request).get();
      System.out.printf("TPU VM created: %s\n", response.getName());
    }
  }
}
//[END tpu_vm_create]
