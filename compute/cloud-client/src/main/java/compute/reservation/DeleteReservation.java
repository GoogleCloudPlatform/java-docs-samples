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

// [START compute_reservation_delete]

import com.google.cloud.compute.v1.DeleteReservationRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.ReservationsClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeleteReservation {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the reservation you want to delete.
    String reservationName = "YOUR_RESERVATION_NAME";
    // Name of the zone.
    String zone = "us-central1-a";

    deleteReservation(projectId, zone, reservationName);
  }

  // Delete a reservation from the project.
  public static void deleteReservation(String projectId, String zone, String reservationName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    /* Initialize client that will be used to send requests. This client only needs to be created
       once, and can be reused for multiple requests. */
    try (ReservationsClient reservationsClient = ReservationsClient.create()) {

      DeleteReservationRequest deleteReservationRequest = DeleteReservationRequest.newBuilder()
          .setProject(projectId)
          .setZone(zone)
          .setReservation(reservationName)
          .build();

      Operation response = reservationsClient.deleteAsync(
          deleteReservationRequest).get(5, TimeUnit.MINUTES);

      if (response.getStatus() == Operation.Status.DONE) {
        System.out.println("Deleted reservation: " + reservationName);
      }
    }
  }
}
// [END compute_reservation_delete]