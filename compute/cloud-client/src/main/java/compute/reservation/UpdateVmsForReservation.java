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

// [START compute_reservation_vms_update]

import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.ReservationsClient;
import com.google.cloud.compute.v1.ReservationsResizeRequest;
import com.google.cloud.compute.v1.ResizeReservationRequest;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UpdateVmsForReservation {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // The zone where the reservation is located.
    String zone =  "us-central1-a";
    // Name of the reservation to update.
    String reservationName = "YOUR_RESERVATION_NAME";
    // Number of instances to update in the reservation.
    int numberOfVms = 3;

    updateVmsForReservation(projectId, zone, reservationName, numberOfVms);
  }

  // Updates a reservation with new VM Capacity.
  public static void updateVmsForReservation(
      String projectId, String zone, String reservationName, int numberOfVms)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ReservationsClient reservationsClient = ReservationsClient.create()) {

      ResizeReservationRequest resizeReservationRequest =
          ResizeReservationRequest.newBuilder()
          .setProject(projectId)
          .setZone(zone)
          .setReservation(reservationName)
          .setReservationsResizeRequestResource(ReservationsResizeRequest.newBuilder()
              .setSpecificSkuCount(numberOfVms)
              .build())
          .build();

      Operation response = reservationsClient.resizeAsync(resizeReservationRequest)
          .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Reservation update failed !!" + response);
        return;
      }
      System.out.println("Reservation updated successfully: " + response.getStatus());
    }
  }
}
// [END compute_reservation_vms_update]