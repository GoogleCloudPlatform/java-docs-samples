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

//[START tpu_vm_stop]
import com.google.cloud.tpu.v2.Node;
import com.google.cloud.tpu.v2.NodeName;
import com.google.cloud.tpu.v2.StopNodeRequest;
import com.google.cloud.tpu.v2.TpuClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class StopTpuVm {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // The zone where the TPU is located.
    // For more information about supported TPU types for specific zones,
    // see https://cloud.google.com/tpu/docs/regions-zones
    String zone = "us-central1-f";
    // The name for your TPU.
    String nodeName = "YOUR_TPU_NAME";

    stopTpuVm(projectId, zone, nodeName);
  }

  // Stops a TPU VM with the specified name in the given project and zone.
  public static Node stopTpuVm(String projectId, String zone, String nodeName)
      throws IOException, ExecutionException, InterruptedException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (TpuClient tpuClient = TpuClient.create()) {
      String name = NodeName.of(projectId, zone, nodeName).toString();

      StopNodeRequest request = StopNodeRequest.newBuilder().setName(name).build();

      return tpuClient.stopNodeAsync(request).get();
    }
  }
}
//[END tpu_vm_stop]

