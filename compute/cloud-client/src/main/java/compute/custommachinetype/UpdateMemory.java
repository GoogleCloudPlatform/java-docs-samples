/*
 * Copyright 2022 Google LLC
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

package compute.custommachinetype;

// [START compute_custom_machine_type_update_memory]

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.GetInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.Instance.Status;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.InstancesSetMachineTypeRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.SetMachineTypeInstanceRequest;
import com.google.cloud.compute.v1.StopInstanceRequest;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UpdateMemory {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "your-google-cloud-project-id";
    // Name of the zone to create the instance in. For example: "us-west3-b".
    String zone = "gcloud-zone";
    // Name of the new virtual machine (VM) instance.
    String instanceName = "instance-name";
    // The amount of memory for the VM instance, in megabytes.
    int newMemory = 256;

    modifyInstanceWithExtendedMemory(projectId, zone, instanceName, newMemory);
  }

  // Modify an existing VM to use extended memory.
  public static Instance modifyInstanceWithExtendedMemory(String project, String zone,
      String instanceName, int newMemory)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {

    try (InstancesClient instancesClient = InstancesClient.create()) {

      // Prepare the request to insert an instance.
      GetInstanceRequest getInstanceRequest = GetInstanceRequest.newBuilder()
          .setProject(project)
          .setZone(zone)
          .setInstance(instanceName)
          .build();

      Instance instance = instancesClient.get(getInstanceRequest);

      // Check the machine type.
      if (!(instance.getMachineType().contains("machineTypes/n1-") || instance.getMachineType()
          .contains("machineTypes/n2-") || instance.getMachineType()
          .contains("machineTypes/n2d-"))) {
        System.out.println("extra memory is available only for N1, N2 and N2D CPUs");
        return Instance.getDefaultInstance();
      }

      // Make sure that the machine is turned off.
      if (!(instance.getStatus().equals(Status.TERMINATED.toString()) ||
          instance.getStatus().equals(Status.STOPPED.toString()))) {

        StopInstanceRequest stopInstanceRequest = StopInstanceRequest.newBuilder()
            .setProject(project)
            .setZone(zone)
            .setInstance(instanceName)
            .build();

        OperationFuture<Operation, Operation> operation = instancesClient.stopAsync(
            stopInstanceRequest);
        Operation response = operation.get(3, TimeUnit.MINUTES);
        if (response.hasError()) {
          System.out.printf("Unable to stop instance %s", response.getError());
          return Instance.getDefaultInstance();
        }
      }

      // Modify the machine definition, remember that extended memory
      // is available only for N1, N2 and N2D CPUs.
      String machineType = instance.getMachineType();
      String start = machineType.substring(0, machineType.lastIndexOf("-"));

      // Prepare the request.
      SetMachineTypeInstanceRequest setMachineTypeInstanceRequest = SetMachineTypeInstanceRequest.newBuilder()
          .setProject(project)
          .setZone(zone)
          .setInstance(instanceName)
          .setInstancesSetMachineTypeRequestResource(InstancesSetMachineTypeRequest.newBuilder()
              .setMachineType(String.format("%s-%d-ext", start, newMemory))
              .build())
          .build();

      // Wait for the operation to complete.
      Operation response = instancesClient.setMachineTypeAsync(setMachineTypeInstanceRequest)
          .get(3, TimeUnit.MINUTES);

      // Check for errors.
      if (response.hasError()) {
        System.out.printf("Unable to update instance %s", response.getError());
        return Instance.getDefaultInstance();
      }
      System.out.println("Instance updated!");
      return instancesClient.get(project, zone, instanceName);
    }
  }
}
// [END compute_custom_machine_type_update_memory]