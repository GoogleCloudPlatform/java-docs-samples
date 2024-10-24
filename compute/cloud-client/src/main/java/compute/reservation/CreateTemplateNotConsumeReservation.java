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

// [START compute_template_not_consume_reservation]

import static com.google.cloud.compute.v1.ReservationAffinity.ConsumeReservationType.NO_RESERVATION;

import com.google.cloud.compute.v1.AccessConfig;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.InsertInstanceTemplateRequest;
import com.google.cloud.compute.v1.InstanceProperties;
import com.google.cloud.compute.v1.InstanceTemplate;
import com.google.cloud.compute.v1.InstanceTemplatesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.ReservationAffinity;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateTemplateNotConsumeReservation {
  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the template you want to query.
    String templateName = "YOUR_INSTANCE_TEMPLATE_NAME";

    createTemplateNotConsumeReservation(projectId, templateName);
  }


  // Create a template that explicitly doesn't consume any reservations.
  public static void createTemplateNotConsumeReservation(String projectId, String templateName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (InstanceTemplatesClient instanceTemplatesClient = InstanceTemplatesClient.create()) {

      String machineType = "e2-standard-4";
      String sourceImage = "projects/debian-cloud/global/images/family/debian-11";

      // The template describes the size and source image of the boot disk
      // to attach to the instance.
      AttachedDisk attachedDisk = AttachedDisk.newBuilder()
          .setInitializeParams(AttachedDiskInitializeParams.newBuilder()
              .setSourceImage(sourceImage)
              .setDiskType("pd-balanced")
              .setDiskSizeGb(250).build())
          .setAutoDelete(true)
          .setBoot(true).build();

      // The template connects the instance to the `default` network,
      // without specifying a subnetwork.
      NetworkInterface networkInterface = NetworkInterface.newBuilder()
          .setName("global/networks/default")
          // The template lets the instance use an external IP address.
          .addAccessConfigs(AccessConfig.newBuilder()
              .setName("External NAT")
              .setType(AccessConfig.Type.ONE_TO_ONE_NAT.toString())
              .setNetworkTier(AccessConfig.NetworkTier.PREMIUM.toString()).build()).build();

      // Set reservation affinity to "none"
      ReservationAffinity reservationAffinity =
          ReservationAffinity.newBuilder()
              .setConsumeReservationType(NO_RESERVATION.toString())
              .build();

      InstanceProperties instanceProperties = InstanceProperties.newBuilder()
          .addDisks(attachedDisk)
          .setMachineType(machineType)
          .setReservationAffinity(reservationAffinity)
          .addNetworkInterfaces(networkInterface).build();

      InsertInstanceTemplateRequest insertInstanceTemplateRequest = InsertInstanceTemplateRequest
          .newBuilder()
          .setProject(projectId)
          .setInstanceTemplateResource(InstanceTemplate.newBuilder()
              .setName(templateName)
              .setProperties(instanceProperties).build()).build();

      // Create the Instance Template.
      Operation response = instanceTemplatesClient.insertAsync(insertInstanceTemplateRequest)
          .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Instance Template creation failed ! ! " + response);
        return;
      }
      System.out
          .printf("Instance Template Operation Status %s: %s", templateName, response.getStatus());
    }
  }
}
// [END compute_template_not_consume_reservation]
