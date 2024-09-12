/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package compute.reservation;

// [START compute_reservation_list]

import com.google.cloud.compute.v1.Reservation;
import com.google.cloud.compute.v1.ReservationsClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListReservations {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String project = "YOUR_PROJECT_ID";
    // Zone in which reservations are located.
    String zone = "us-central1-a";

    listReservations(project, zone);
  }

  // List all reservations in the given project and zone.
  public static List<Reservation> listReservations(String project, String zone) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    List<Reservation> listOfReservations = new ArrayList<>();

    try (ReservationsClient reservationsClient = ReservationsClient.create()) {
      for (Reservation reservation : reservationsClient.list(project, zone).iterateAll()) {
        listOfReservations.add(reservation);
        System.out.println("Reservation: " + reservation.getName());
      }
    }
    return listOfReservations;
  }
}
// [END compute_reservation_list]