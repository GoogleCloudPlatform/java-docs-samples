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

//[START tpu_vm_get]

import com.google.cloud.tpu.v2.GetNodeRequest;
import com.google.cloud.tpu.v2.Node;
import com.google.cloud.tpu.v2.NodeName;
import com.google.cloud.tpu.v2.TpuClient;
import java.io.IOException;

public class GetTpuVm {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "YOUR_PROJECT_ID";
    String zone = "europe-west4-a";
    String tpuVmName = "YOUR_TPU_NAME";

    getTpuVm(projectId, zone, tpuVmName);
  }

  // Describes a TPU VM with the specified name in the given project and zone.
  public static Node getTpuVm(String projectId, String zone, String tpuVmName)
      throws IOException {
    Node node;
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (TpuClient tpuClient = TpuClient.create()) {
      String nodeName = NodeName.of(projectId, zone, tpuVmName).toString();

      GetNodeRequest request = GetNodeRequest.newBuilder().setName(nodeName).build();
      node = tpuClient.getNode(request);
    }
    return node;
  }
}
//[END tpu_vm_get]
