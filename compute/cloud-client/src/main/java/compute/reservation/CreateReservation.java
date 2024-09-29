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

import com.google.cloud.compute.v1.AcceleratorConfig;
import com.google.cloud.compute.v1.AllocationSpecificSKUAllocationAllocatedInstancePropertiesReservedDisk;
import com.google.cloud.compute.v1.AllocationSpecificSKUAllocationReservedInstanceProperties;
import com.google.cloud.compute.v1.AllocationSpecificSKUReservation;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Reservation;
import com.google.cloud.compute.v1.ReservationsClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// [START compute_reservation_create]
public class CreateReservation {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the zone in which you want to create the disk.
    String zone = "us-central1-a";
    // Name of the reservation you want to create.
    String reservationName = "YOUR_RESERVATION_NAME";
    // Number of instances in the reservation.
    int numberOfVms = 3;

    createReservation(projectId, reservationName, numberOfVms, zone);
  }

  // Creates reservation with optional flags
  public static void createReservation(
      String projectId, String reservationName, int numberOfVms, String zone)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ReservationsClient reservationsClient = ReservationsClient.create()) {

      // Create the reservation with optional properties:
      // Machine type of the instances in the reservation.
      String machineType = "n1-standard-2";
      // Number of accelerators to be attached to the instances in the reservation.
      int numberOfAccelerators = 1;
      // Accelerator type to be attached to the instances in the reservation.
      String acceleratorType = "nvidia-tesla-t4";
      // Minimum CPU platform to be attached to the instances in the reservation.
      String minCpuPlatform = "Intel Skylake";
      // Local SSD size in GB to be attached to the instances in the reservation.
      int localSsdSize = 375;
      // Local SSD interfaces to be attached to the instances in the reservation.
      String localSsdInterface1 = "NVME";
      String localSsdInterface2 = "SCSI";
      boolean specificReservationRequired = true;

      Reservation reservation =
          Reservation.newBuilder()
              .setName(reservationName)
              .setZone(zone)
              .setSpecificReservationRequired(specificReservationRequired)
              .setSpecificReservation(
                  AllocationSpecificSKUReservation.newBuilder()
                      // Set the number of instances
                      .setCount(numberOfVms)
                      // Set instance properties
                      .setInstanceProperties(
                          AllocationSpecificSKUAllocationReservedInstanceProperties.newBuilder()
                              .setMachineType(machineType)
                              .setMinCpuPlatform(minCpuPlatform)
                              .addGuestAccelerators(
                                  AcceleratorConfig.newBuilder()
                                      .setAcceleratorCount(numberOfAccelerators)
                                      .setAcceleratorType(acceleratorType)
                                      .build())
                              .addLocalSsds(
                            AllocationSpecificSKUAllocationAllocatedInstancePropertiesReservedDisk
                                      .newBuilder()
                                      .setDiskSizeGb(localSsdSize)
                                      .setInterface(localSsdInterface1)
                                      .build())
                              .addLocalSsds(
                            AllocationSpecificSKUAllocationAllocatedInstancePropertiesReservedDisk
                                      .newBuilder()
                                      .setDiskSizeGb(localSsdSize)
                                      .setInterface(localSsdInterface2)
                                      .build())
                              .build())
                      .build())
              .build();

      // Wait for the create reservation operation to complete.
      Operation response =
          reservationsClient.insertAsync(projectId, zone, reservation).get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Reservation creation failed!" + response);
        return;
      }
      System.out.println("Reservation created. Operation Status: " + response.getStatus());
    }
  }
}
// [END compute_reservation_create]