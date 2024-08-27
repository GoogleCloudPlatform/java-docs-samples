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

import com.google.cloud.compute.v1.AllocationSpecificSKUReservation;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Reservation;
import com.google.cloud.compute.v1.ReservationsClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// [START compute_reservation_create_for_regional_instance_template]

public class CreateReservationWithRegionalInstanceTemplate {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the zone in which you want to create the reservation.
    String zone = "us-central1-a";
    // Name of the reservation you want to create.
    String reservationName = "YOUR_RESERVATION_NAME";
    // Name of your instance template.
    String instanceTemplateName = "INSTANCE_TEMPLATE_NAME";
    // The number of virtual machines you want to create.
    int numberOfVms = 3;

    createReservationWithRegionalInstanceTemplate(
        projectId, reservationName, instanceTemplateName, numberOfVms, zone);
  }

  // Creates a reservation in a project for the instance template with regional location.
  public static void createReservationWithRegionalInstanceTemplate(
      String projectId,
      String reservationName,
      String instanceTemplateName,
      int numberOfVms,
      String zone)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ReservationsClient reservationsClient = ReservationsClient.create()) {

      String region = zone.substring(0, zone.lastIndexOf('-')); // Extract the region from the zone
      String instanceTemplateUri =
          String.format("projects/%s/regions/%s/instanceTemplates/%s",
              projectId, region, instanceTemplateName);

      // Create the reservation.
      Reservation reservation =
          Reservation.newBuilder()
              .setName(reservationName)
              .setZone(zone)
              .setSpecificReservation(
                  AllocationSpecificSKUReservation.newBuilder()
                      // Set the number of instances
                      .setCount(numberOfVms)
                      // Set the instance template to be used for creating the reservation.
                      .setSourceInstanceTemplate(instanceTemplateUri)
                      .build())
              .build();

      // Wait for the create reservation operation to complete.
      Operation response =
          reservationsClient.insertAsync(projectId, zone, reservation)
              .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Reservation creation failed!" + response);
        return;
      }
      System.out.println("Reservation created. Operation Status: " + response.getStatus());
    }
  }
}
// [END compute_reservation_create_for_regional_instance_template]