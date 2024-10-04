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

// [START compute_consume_single_project_reservation]

import static com.google.cloud.compute.v1.ReservationAffinity.ConsumeReservationType.SPECIFIC_RESERVATION;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.AllocationSpecificSKUAllocationReservedInstanceProperties;
import com.google.cloud.compute.v1.AllocationSpecificSKUReservation;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Reservation;
import com.google.cloud.compute.v1.ReservationAffinity;
import com.google.cloud.compute.v1.ReservationsClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ConsumeSingleProjectReservation {
  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the zone in which you want to create the reservation.
    String zone = "us-central1-a";
    // Name of the reservation you want to create.
    String reservationName = "YOUR_RESERVATION_NAME";
    // Name of the VM instance you want to query.
    String instanceName = "YOUR_INSTANCE_NAME";
    // Number of instances in the reservation.
    int numberOfVms = 10;
    // Machine type of the instances in the reservation.
    String machineType = "n2-standard-32";
    // Minimum CPU platform to be attached to the instances in the reservation.
    String minCpuPlatform = "Intel Cascade Lake";
    boolean specificReservationRequired = true;

    createReservation(projectId, reservationName, numberOfVms,
        zone, machineType, minCpuPlatform, specificReservationRequired);
    createInstance(projectId, zone, instanceName, machineType, minCpuPlatform, reservationName);
  }

  // Creates reservation
  public static void createReservation(
      String projectId, String reservationName, int numberOfVms, String zone,
      String machineType, String minCpuPlatform, boolean specificReservationRequired)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ReservationsClient reservationsClient = ReservationsClient.create()) {

      Reservation reservation =
          Reservation.newBuilder()
              .setName(reservationName)
              .setZone(zone)
              .setSpecificReservationRequired(specificReservationRequired)
              .setSpecificReservation(
                  AllocationSpecificSKUReservation.newBuilder()
                      .setCount(numberOfVms)
                      .setInstanceProperties(
                          AllocationSpecificSKUAllocationReservedInstanceProperties.newBuilder()
                              .setMachineType(machineType)
                              .setMinCpuPlatform(minCpuPlatform)
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

  // Create a new instance with the provided "instanceName" value in the specified project and zone.
  public static void createInstance(String project, String zone,
      String instanceName, String machineType, String minCpuPlatform, String reservationName)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // Below are sample values that can be replaced.
    // machineType: machine type of the VM being created.
    // *   This value uses the format zones/{zone}/machineTypes/{type_name}.
    // *   For a list of machine types, see https://cloud.google.com/compute/docs/machine-types
    // sourceImage: path to the operating system image to mount.
    // *   For details about images you can mount, see https://cloud.google.com/compute/docs/images
    // diskSizeGb: storage size of the boot disk to attach to the instance.
    // networkName: network interface to associate with the instance.
    // String machineType = String.format("zones/%s/machineTypes/n1-standard-1", zone);
    String sourceImage = String
        .format("projects/debian-cloud/global/images/family/%s", "debian-11");
    long diskSizeGb = 10L;
    String networkName = "default";

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `instancesClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (InstancesClient instancesClient = InstancesClient.create()) {
      // Instance creation requires at least one persistent disk and one network interface.
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

      // Use the network interface provided in the networkName argument.
      NetworkInterface networkInterface = NetworkInterface.newBuilder()
          .setName(networkName)
          .build();

      Instance instanceResource =
          Instance.newBuilder()
              .setName(instanceName)
              .setMachineType(String.format("zones/%s/machineTypes/%s", zone, machineType))
              .addDisks(disk)
              .addNetworkInterfaces(networkInterface)
              .setMinCpuPlatform(minCpuPlatform)
              // Set Reservation Affinity
              .setReservationAffinity(
                  ReservationAffinity.newBuilder()
                      .setConsumeReservationType(
                          SPECIFIC_RESERVATION.name())
                      .setKey("compute.googleapis.com/reservation-name")
                      // Set specific reservation
                      .addValues(reservationName)
                      .build())
              .build();

      System.out.printf("Creating instance: %s at %s %n", instanceName, zone);

      // Insert the instance in the specified project and zone.
      InsertInstanceRequest insertInstanceRequest = InsertInstanceRequest.newBuilder()
          .setProject(project)
          .setZone(zone)
          .setInstanceResource(instanceResource)
          .build();

      OperationFuture<Operation, Operation> operation = instancesClient.insertAsync(
          insertInstanceRequest);

      // Wait for the operation to complete.
      Operation response = operation.get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Instance creation failed ! ! " + response);
        return;
      }
      System.out.println("Operation Status: " + response.getStatus());
    }
  }
}
// [END compute_consume_single_project_reservation]
