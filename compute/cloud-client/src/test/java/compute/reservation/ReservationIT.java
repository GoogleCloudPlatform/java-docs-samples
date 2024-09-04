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

import static com.google.common.truth.Truth.assertThat;
import static compute.Util.getZone;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.compute.v1.AllocationSpecificSKUAllocationReservedInstanceProperties;
import com.google.cloud.compute.v1.AllocationSpecificSKUReservation;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Reservation;
import com.google.cloud.compute.v1.ReservationsClient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 10, unit = TimeUnit.MINUTES)
public class ReservationIT {

  private static String PROJECT_ID;
  private static String ZONE;
  private static String RESERVATION_NAME;

  private ByteArrayOutputStream stdOut;

  @BeforeAll
  public static void setUp()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
    ZONE = getZone();
    RESERVATION_NAME = "test-reservation-" + UUID.randomUUID();

    // Create a reservation
    ReservationIT.createReservation(PROJECT_ID, ZONE, RESERVATION_NAME);

    assertThat(stdOut.toString()).contains("Reservation created. Operation Status: DONE");
    stdOut.close();
    System.setOut(out);
  }

  @AfterAll
  public static void cleanup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    // Delete all instances created for testing.
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_NAME);
    assertThat(stdOut.toString()).contains("Deleted reservation: " + RESERVATION_NAME);
    try (ReservationsClient reservationsClient = ReservationsClient.create()) {
      // Get the reservation.
      Assertions.assertThrows(
          NotFoundException.class,
          () -> reservationsClient.get(PROJECT_ID, ZONE, RESERVATION_NAME));
    }

    stdOut.close();
    System.setOut(out);
  }

  @BeforeEach
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @AfterEach
  public void afterEach() {
    stdOut = null;
    System.setOut(null);
  }

  @Test
  public void testGetReservation()
      throws IOException {
    Reservation reservation = GetComputeReservation.getReservation(
        PROJECT_ID, RESERVATION_NAME, ZONE);

    assertNotNull(reservation);
    assertThat(reservation.getName()).isEqualTo(RESERVATION_NAME);
  }

  public static void createReservation(String projectId, String zone, String reservationName)
      throws ExecutionException, InterruptedException, TimeoutException, IOException {
    // Create the reservation.
    try (ReservationsClient reservationsClient = ReservationsClient.create()) {

      Reservation reservation = Reservation.newBuilder()
          .setName(reservationName)
          .setSpecificReservation(
              AllocationSpecificSKUReservation.newBuilder()
                  .setCount(1)
                  .setInstanceProperties(
                      AllocationSpecificSKUAllocationReservedInstanceProperties.newBuilder()
                          .setMachineType("n1-standard-1")
                          .build())
                  .build())
          .build();

      Operation response =
          reservationsClient.insertAsync(projectId, zone, reservation).get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Reservation creation failed!" + response);
      }
      System.out.println("Reservation created. Operation Status: " + response.getStatus());
    }
  }
}