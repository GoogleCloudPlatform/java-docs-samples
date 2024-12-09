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

//[START tpu_vm_list]
import com.google.cloud.tpu.v2.ListNodesRequest;
import com.google.cloud.tpu.v2.TpuClient;
import java.io.IOException;

public class ListTpuVms {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // The zone where the TPUs are located.
    // For more information about supported TPU types for specific zones,
    // see https://cloud.google.com/tpu/docs/regions-zones
    String zone = "us-central1-f";

    listTpuVms(projectId, zone);
  }

  // Lists TPU VMs in the specified zone.
  public static TpuClient.ListNodesPage listTpuVms(String projectId, String zone)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (TpuClient tpuClient = TpuClient.create()) {
      String parent = String.format("projects/%s/locations/%s", projectId, zone);

      ListNodesRequest request = ListNodesRequest.newBuilder().setParent(parent).build();

      return tpuClient.listNodes(request).getPage();
    }
  }
}
//[END tpu_vm_list]
