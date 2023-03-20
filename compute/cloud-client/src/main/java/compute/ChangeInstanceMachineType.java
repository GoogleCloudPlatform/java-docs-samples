/*
 * Copyright 2023 Google LLC
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

package compute;

// [START compute_change_machine_type]

import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.Instance.Status;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.InstancesSetMachineTypeRequest;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ChangeInstanceMachineType {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "your-project-id";
    // Name of the zone your instance belongs to.
    String zone = "zone-name";
    // Name of the VM you want to modify.
    String instanceName = "instance-name";
    // The new machine type you want to use for the VM.
    // For example: "e2-standard-8", "e2-custom-4-2048" or "m1-ultramem-40"
    // More about machine types: https://cloud.google.com/compute/docs/machine-resource
    String newMachineType = "e2-standard-8";
    changeMachineType(projectId, zone, instanceName, newMachineType);
  }

  // Changes the machine type of VM.
  // The VM needs to be in the 'TERMINATED' state for this operation to be successful.
  public static void changeMachineType(String projectId, String zone, String instanceName,
      String newMachineType)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `instancesClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (InstancesClient instancesClient = InstancesClient.create()) {

      Instance instance = instancesClient.get(projectId, zone, instanceName);
      if (!instance.getStatus().equals(Status.TERMINATED.name())) {
        throw new Error(String.format(
            "Only machines in TERMINATED state can have their machine type changed. "
                + "%s is in %s state.", instance.getName(), instance.getStatus()));
      }

      InstancesSetMachineTypeRequest machineTypeRequest =
          InstancesSetMachineTypeRequest.newBuilder()
              .setMachineType(String.format("projects/%s/zones/%s/machineTypes/%s",
                  projectId, zone, newMachineType))
              .build();

      Operation response = instancesClient
          .setMachineTypeAsync(projectId, zone, instanceName, machineTypeRequest)
          .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Machine type update failed! " + response);
        return;
      }
      System.out.println("Machine type update - operation status: " + response.getStatus());
    }
  }
}
// [END compute_change_machine_type]