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

package compute.tpu;

//[START tpu_vm_delete]

import com.google.cloud.tpu.v2.DeleteNodeRequest;
import com.google.cloud.tpu.v2.NodeName;
import com.google.cloud.tpu.v2.TpuClient;
import com.google.protobuf.Empty;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class DeleteTpuVm {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "YOUR_PROJECT_ID";
    String zone = "europe-west4-a";
    String tpuVmName = "YOUR_TPU_NAME";

    deleteTpuVm(projectId, zone, tpuVmName);
  }

  // Deletes a TPU VM with the specified name in the given project and zone.
  public static void deleteTpuVm(String projectId, String zone, String tpuVmName)
      throws IOException, ExecutionException, InterruptedException {
    try (TpuClient tpuClient = TpuClient.create()) {
      String nodeName = NodeName.of(projectId, zone, tpuVmName).toString();

      DeleteNodeRequest request = DeleteNodeRequest.newBuilder().setName(nodeName).build();
      Empty response = tpuClient.deleteNodeAsync(request).get();

      System.out.printf("TPU VM deleted: %s\n", response);
    }
  }
}
//[END tpu_vm_delete]
