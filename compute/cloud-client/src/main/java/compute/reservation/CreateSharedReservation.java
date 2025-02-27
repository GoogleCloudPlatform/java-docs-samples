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
import com.google.cloud.compute.v1.InsertReservationRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import com.google.cloud.compute.v1.Reservation;
import com.google.cloud.compute.v1.ReservationsClient;
import com.google.cloud.compute.v1.ShareSettings;
import com.google.cloud.compute.v1.ShareSettingsProjectConfig;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateSharedReservation {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // The ID of the project where you want to reserve resources
    // and where the instance template exists.
    // By default, no projects are allowed to create or modify shared reservations
    // in an organization. Add projects to the Shared Reservations Owner Projects
    // (compute.sharedReservationsOwnerProjects) organization policy constraint
    // to allow them to create and modify shared reservations.
    // For more information visit this page:
    // https://cloud.google.com/compute/docs/instances/reservations-shared#shared_reservation_constraint
    String projectId = "YOUR_PROJECT_ID";
    // Zone in which to reserve resources.
    String zone = "us-central1-a";
    // Name of the reservation to be created.
    String reservationName = "YOUR_RESERVATION_NAME";
    // The URI of the global instance template to be used for creating the reservation.
    String instanceTemplateUri = String.format(
        "projects/%s/global/instanceTemplates/%s", projectId, "YOUR_INSTANCE_TEMPLATE_NAME");
    // Number of instances for which capacity needs to be reserved.
    int vmCount = 3;

    createSharedReservation(projectId, zone, reservationName, instanceTemplateUri, vmCount);
  }

  // Creates a shared reservation with the given name in the given zone.
  public static Status createSharedReservation(
          String projectId, String zone,
          String reservationName, String instanceTemplateUri, int vmCount)
          throws ExecutionException, InterruptedException, TimeoutException, IOException {

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ReservationsClient reservationsClient = ReservationsClient.create()) {
      ShareSettings shareSettings = ShareSettings.newBuilder()
              .setShareType(String.valueOf(ShareSettings.ShareType.SPECIFIC_PROJECTS))
              // The IDs of projects that can consume this reservation. You can include up to
              // 100 consumer projects. These projects must be in the same organization as
              // the owner project. Don't include the owner project.
              // By default, it is already allowed to consume the reservation.
              .putProjectMap("CONSUMER_PROJECT_1", ShareSettingsProjectConfig.newBuilder().build())
              .putProjectMap("CONSUMER_PROJECT_2", ShareSettingsProjectConfig.newBuilder().build())
              .build();

      Reservation reservationResource =
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

      InsertReservationRequest request =
              InsertReservationRequest.newBuilder()
                      .setProject(projectId)
                      .setZone(zone)
                      .setReservationResource(reservationResource)
                      .build();

      Operation response = reservationsClient.insertAsync(request)
              .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        throw new Error("Reservation creation failed!!" + response);
      }
      return response.getStatus();
    }
  }
}
// [END compute_reservation_create_shared]
