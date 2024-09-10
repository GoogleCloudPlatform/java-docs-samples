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

// [START compute_reservation_create_shared]

import com.google.cloud.compute.v1.AllocationSpecificSKUReservation;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Reservation;
import com.google.cloud.compute.v1.ReservationsClient;
import com.google.cloud.compute.v1.ShareSettings;
import com.google.cloud.compute.v1.ShareSettingsProjectConfig;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateSharedReservation {
  private final ReservationsClient reservationsClient;

  // Constructor to inject the ReservationsClient
  public CreateSharedReservation(ReservationsClient reservationsClient) {
    this.reservationsClient = reservationsClient;
  }

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // The ID of the project where you want to reserve resources
    // and where the instance template exists.
    String projectId = "YOUR_PROJECT_ID";
    // Zone in which the reservation resides.
    String zone = "us-central1-a";
    // Name of the reservation to be created.
    String reservationName = "YOUR_RESERVATION_NAME";
    // The URI of the global instance template to be used for creating the reservation.
    String instanceTemplateUri = String.format(
        "projects/%s/global/instanceTemplates/YOUR_INSTANCE_TEMPLATE_NAME", projectId);
    // Number of instances for which capacity needs to be reserved.
    int vmCount = 3;
    // In your main method, create a real ReservationsClient
    ReservationsClient client = ReservationsClient.create();
    // Create an instance of your class, passing in the real client
    CreateSharedReservation creator = new CreateSharedReservation(client);

    creator.createSharedReservation(projectId, zone, reservationName, instanceTemplateUri, vmCount);
  }

  // Creates a shared reservation with the given name in the given zone.
  public void createSharedReservation(
      String projectId, String zone,
      String reservationName, String instanceTemplateUri, int vmCount)
      throws ExecutionException, InterruptedException, TimeoutException {

    ShareSettings shareSettings = ShareSettings.newBuilder()
        .setShareType(String.valueOf(ShareSettings.ShareType.SPECIFIC_PROJECTS))
        .putProjectMap("CONSUMER_PROJECT_ID_1", ShareSettingsProjectConfig.newBuilder().build())
        .putProjectMap("CONSUMER_PROJECT_ID_2", ShareSettingsProjectConfig.newBuilder().build())
        .build();

    // Create the reservation.
    Reservation reservation =
        Reservation.newBuilder()
            .setName(reservationName)
            .setZone(zone)
            .setSpecificReservationRequired(true)
            .setShareSettings(shareSettings)
            .setSpecificReservation(
                AllocationSpecificSKUReservation.newBuilder()
                    .setCount(vmCount)
                    .setSourceInstanceTemplate(instanceTemplateUri)
                    .build())
            .build();

    // Wait for the create reservation operation to complete.
    Operation response =
        this.reservationsClient.insertAsync(projectId, zone, reservation).get(3, TimeUnit.MINUTES);

    if (response.hasError()) {
      System.out.println("Reservation creation failed!" + response);
      return;
    }
    System.out.println("Reservation created. Operation Status: " + response.getStatus());
  }
}
// [END compute_reservation_create_shared]
