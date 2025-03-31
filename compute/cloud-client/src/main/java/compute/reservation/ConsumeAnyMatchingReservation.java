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

package compute.reservation;

// [START compute_consume_any_matching_reservation]
import static com.google.cloud.compute.v1.ReservationAffinity.ConsumeReservationType.ANY_RESERVATION;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.ReservationAffinity;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ConsumeAnyMatchingReservation {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Zone where the VM instance will be created.
    String zone = "us-central1-a";
    // Name of the VM instance you want to query.
    String instanceName = "YOUR_INSTANCE_NAME";
    // machineType: machine type of the VM being created.
    // *   For a list of machine types, see https://cloud.google.com/compute/docs/machine-types
    String machineTypeName = "n1-standard-4";
    // sourceImage: path to the operating system image to mount.
    // *   For details about images you can mount, see https://cloud.google.com/compute/docs/images
    String sourceImage = "projects/debian-cloud/global/images/family/debian-11";
    // diskSizeGb: storage size of the boot disk to attach to the instance.
    long diskSizeGb = 10L;
    // networkName: network interface to associate with the instance.
    String networkName = "default";
    // Minimum CPU platform of the instances.
    String minCpuPlatform = "Intel Skylake";

    createInstanceAsync(projectId, zone, instanceName, machineTypeName, sourceImage,
        diskSizeGb, networkName, minCpuPlatform);
  }

  // Create a virtual machine targeted with the reserveAffinity field.
  // In this consumption model, existing and new VMs automatically consume a reservation
  // if their properties match the VM properties specified in the reservation.
  public static Instance createInstanceAsync(String projectId, String zone,
      String instanceName, String machineTypeName, String sourceImage,
      long diskSizeGb, String networkName, String minCpuPlatform)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    String machineType = String.format("zones/%s/machineTypes/%s", zone, machineTypeName);
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (InstancesClient instancesClient = InstancesClient.create()) {
      AttachedDisk disk =
          AttachedDisk.newBuilder()
              .setBoot(true)
              .setAutoDelete(true)
              .setType(AttachedDisk.Type.PERSISTENT.toString())
              .setDeviceName("disk-1")
              .setInitializeParams(
                  AttachedDiskInitializeParams.newBuilder()
                      .setSourceImage(sourceImage)
                      .setDiskSizeGb(diskSizeGb)
                      .build())
              .build();

      NetworkInterface networkInterface = NetworkInterface.newBuilder()
          .setName(networkName)
          .build();

      ReservationAffinity reservationAffinity =
          ReservationAffinity.newBuilder()
              .setConsumeReservationType(ANY_RESERVATION.toString())
              .build();

      Instance instanceResource =
          Instance.newBuilder()
              .setName(instanceName)
              .setMachineType(machineType)
              .addDisks(disk)
              .addNetworkInterfaces(networkInterface)
              .setMinCpuPlatform(minCpuPlatform)
              .setReservationAffinity(reservationAffinity)
              .build();

      InsertInstanceRequest insertInstanceRequest = InsertInstanceRequest.newBuilder()
          .setProject(projectId)
          .setZone(zone)
          .setInstanceResource(instanceResource)
          .build();

      OperationFuture<Operation, Operation> operation = instancesClient.insertAsync(
          insertInstanceRequest);

      Operation response = operation.get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        return null;
      }
      return instancesClient.get(projectId, zone, instanceName);
    }
  }
}
// [END compute_consume_any_matching_reservation]