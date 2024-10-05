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

// [START compute_instance_not_consume_reservation]

import static com.google.cloud.compute.v1.ReservationAffinity.ConsumeReservationType.NO_RESERVATION;

import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.ReservationAffinity;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class CreateInstanceNotConsumeReservation {
  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the zone in which you want to create the reservation.
    String zone = "us-central1-a";
    // Name of the VM instance you want to query.
    String instanceName = "YOUR_INSTANCE_NAME";
    // Machine type of the instances in the reservation.
    String machineType = "n2-standard-32";

    createInstanceNotConsumeReservation(projectId, zone, instanceName, machineType);
  }

  // Create a virtual machine that explicitly doesn't consume reservations
  public static void createInstanceNotConsumeReservation(
      String projectId, String zone, String instanceName, String machineType)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // Below are sample values that can be replaced.
    // sourceImage: path to the operating system image to mount.
    // *   For details about images you can mount, see https://cloud.google.com/compute/docs/images
    // Network interface to associate with the instance.
    String sourceImage = String
        .format("projects/debian-cloud/global/images/family/%s", "debian-11");
    String network = "global/networks/default"; // Example network

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (InstancesClient instancesClient = InstancesClient.create()) {

      // Create the attached disk object
      AttachedDisk attachedDisk =
          AttachedDisk.newBuilder()
              .setBoot(true)
              .setAutoDelete(true)
              .setInitializeParams(
                  AttachedDiskInitializeParams.newBuilder()
                      .setSourceImage(sourceImage)
                      .build())
              .build();

      // Create the network interface object
      NetworkInterface networkInterface =
          NetworkInterface.newBuilder().setName(network).build();

      // Set reservation affinity to "none"
      ReservationAffinity reservationAffinity =
          ReservationAffinity.newBuilder()
              .setConsumeReservationType(NO_RESERVATION.toString())
              .build();

      // Create the instance object
      Instance instance =
          Instance.newBuilder()
              .setName(instanceName)
              .setMachineType("zones/" + zone + "/machineTypes/" + machineType)
              .addDisks(attachedDisk)
              .addNetworkInterfaces(networkInterface)
              .setReservationAffinity(reservationAffinity)
              .build();

      // Wait for the operation to complete.
      Operation response = instancesClient.insertAsync(
          projectId, zone, instance).get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Instance creation failed ! ! " + response);
        return;
      }
      System.out.println("Operation Status: " + response.getStatus());
    }
  }
}
// [END compute_instance_not_consume_reservation]
