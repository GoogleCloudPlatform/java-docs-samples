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
import com.google.cloud.compute.v1.InstanceTemplate;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Reservation;
import com.google.cloud.compute.v1.ReservationsClient;
import com.google.cloud.compute.v1.RegionInstanceTemplatesClient;
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
    String reservationName = "test-disk-name";
    // Machine type of the instances in the reservation.
    String machineType = "n2-standard-4";
    int numberOfAccelerators = 2;
    String acceleratorType = "nvidia-tesla-k80";
    String minCpuPlatform = "Intel Cascade Lake";
    long localSsdSizeGb = 375;
    String localSsdInterface1 = "NVME";
    String localSsdInterface2 = "SCSI";

    long numberOfVms = 3;

    createReservationWithRegion(projectId, reservationName, machineType, numberOfVms, zone, numberOfAccelerators, acceleratorType, minCpuPlatform, localSsdSizeGb,
        localSsdInterface1,
        localSsdInterface2);
  }

  // Creates a reservation in a project for the Instance Template with regional location.
  public static void createReservationWithRegion(
      String projectId,
      String reservationName,
      String machineType,
      long numberOfVms,
      String zone,
      int numberOfAccelerators,
      String acceleratorType,
      String minCpuPlatform, long localSsdSizeGb,
      String localSsdInterface1,
      String localSsdInterface2)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `reservationsClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (ReservationsClient reservationsClient = ReservationsClient.create()) {

      String region = zone.substring(0, zone.lastIndexOf('-')); // Extract the region from the zone


      // Create the reservation.
      Reservation reservation =
          Reservation.newBuilder()
              .setName(reservationName)
              .setZone(zone)
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
                                      .setAcceleratorType(
                                          String.format(
                                              "projects/%s/zones/%s/acceleratorTypes/%s",
                                              projectId, zone, acceleratorType))
                                      .build())
                              .build())
                      .addLocalSsds(
                          AllocationSpecificSKUAllocationAllocatedInstancePropertiesReservedDisk
                              .newBuilder()
                              .setDiskSizeGb(localSsdSizeGb)
                              .setInterface(localSsdInterface1)
                              .build())
                      .addLocalSsds(
                          AllocationSpecificSKUAllocationAllocatedInstancePropertiesReservedDisk
                              .newBuilder()
                              .setDiskSizeGb(localSsdSizeGb)
                              .setInterface(localSsdInterface2)
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