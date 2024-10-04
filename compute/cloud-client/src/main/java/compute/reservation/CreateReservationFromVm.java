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

// [START create_compute_reservation_from_vm]

import com.google.cloud.compute.v1.AcceleratorConfig;
import com.google.cloud.compute.v1.AllocationSpecificSKUAllocationAllocatedInstancePropertiesReservedDisk;
import com.google.cloud.compute.v1.AllocationSpecificSKUAllocationReservedInstanceProperties;
import com.google.cloud.compute.v1.AllocationSpecificSKUReservation;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.InsertReservationRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Reservation;
import com.google.cloud.compute.v1.ReservationsClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateReservationFromVm {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String project = "YOUR_PROJECT_ID";
    // The zone of the VM. In this zone the reservation will be created.
    String zone = "us-central1-a";
    // The name of the reservation to create.
    String reservationName = "YOUR_RESERVATION_NAME";
    // The name of the VM to create the reservation from.
    String vmName = "YOUR_VM_NAME";

    createComputeReservationFromVm(project, zone, reservationName, vmName);
  }

  // Creates a compute reservation in GCP from an existing VM.
  public static void createComputeReservationFromVm(
      String project, String zone, String reservationName, String vmName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (InstancesClient instancesClient = InstancesClient.create();
         ReservationsClient reservationsClient = ReservationsClient.create()) {
      Instance existingVm = instancesClient.get(project, zone, vmName);

      // Extract properties from the existing VM
      List<AcceleratorConfig> guestAccelerators = new ArrayList<>();
      if (!existingVm.getGuestAcceleratorsList().isEmpty()) {
        for (AcceleratorConfig a : existingVm.getGuestAcceleratorsList()) {
          guestAccelerators.add(
              AcceleratorConfig.newBuilder()
                  .setAcceleratorCount(a.getAcceleratorCount())
                  .setAcceleratorType(a.getAcceleratorType()
                      .substring(a.getAcceleratorType().lastIndexOf('/') + 1))
                  .build());
        }
      }

      List<AllocationSpecificSKUAllocationAllocatedInstancePropertiesReservedDisk> localSsds =
          new ArrayList<>();
      if (!existingVm.getDisksList().isEmpty()) {
        for (AttachedDisk disk : existingVm.getDisksList()) {
          if (disk.getDiskSizeGb() >= 375) {
            localSsds.add(
                AllocationSpecificSKUAllocationAllocatedInstancePropertiesReservedDisk.newBuilder()
                    .setDiskSizeGb(disk.getDiskSizeGb())
                    .setInterface(disk.getInterface())
                    .build());
          }
        }
      }

      AllocationSpecificSKUAllocationReservedInstanceProperties instanceProperties =
          AllocationSpecificSKUAllocationReservedInstanceProperties.newBuilder()
              .setMachineType(
                  existingVm.getMachineType()
                      .substring(existingVm.getMachineType().lastIndexOf('/') + 1))
              .setMinCpuPlatform(existingVm.getMinCpuPlatform())
              .addAllLocalSsds(localSsds)
              .addAllGuestAccelerators(guestAccelerators)
              .build();

      Reservation reservation =
          Reservation.newBuilder()
              .setName(reservationName)
              .setSpecificReservation(
                  AllocationSpecificSKUReservation.newBuilder()
                      .setCount(3) // Number of resources that are allocated
                      .setInstanceProperties(instanceProperties)
                      .build())
              .setSpecificReservationRequired(true)
              .build();

      InsertReservationRequest insertReservationRequest =
          InsertReservationRequest.newBuilder()
              .setProject(project)
              .setZone(zone)
              .setReservationResource(reservation)
              .build();

      Operation response = reservationsClient
          .insertAsync(insertReservationRequest).get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Reservation creation failed ! ! " + response);
        return;
      }
      System.out.println("Operation completed successfully.");
    }
  }
}
// [END create_compute_reservation_from_vm]
