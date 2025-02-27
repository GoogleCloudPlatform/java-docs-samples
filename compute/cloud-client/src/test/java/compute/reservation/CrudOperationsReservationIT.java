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
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.compute.v1.Reservation;
import compute.Util;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 6, unit = TimeUnit.MINUTES)
public class CrudOperationsReservationIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "us-central1-a";
  private static final String RESERVATION_NAME = "test-reservation-" + UUID.randomUUID();
  private static final int NUMBER_OF_VMS = 3;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setUp()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    CreateReservation.createReservation(PROJECT_ID, RESERVATION_NAME, NUMBER_OF_VMS, ZONE);
  }

  @AfterAll
  public static void cleanup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Util.cleanUpExistingReservations("test-reservation", PROJECT_ID, ZONE);

    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_NAME);

    // Test that reservation is deleted
    Assertions.assertThrows(
        NotFoundException.class,
        () -> GetReservation.getReservation(PROJECT_ID, RESERVATION_NAME, ZONE));
  }

  @Test
  @Ignore("Issue #9989")
  public void testGetReservation()
      throws IOException {
    Reservation reservation = GetReservation.getReservation(
        PROJECT_ID, RESERVATION_NAME, ZONE);

    assertNotNull(reservation);
    assertThat(reservation.getName()).isEqualTo(RESERVATION_NAME);
  }

  @Test
  @Ignore("Issue #9989")
  public void testListReservation() throws IOException {
    List<Reservation> reservations =
        ListReservations.listReservations(PROJECT_ID, ZONE);

    assertThat(reservations).isNotNull();
    Assert.assertTrue(reservations.get(0).getName().contains("test-"));
  }

  @Test
  @Ignore("Issue #9989")
  public void testUpdateVmsForReservation()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    int newNumberOfVms = 1;
    Reservation reservation = UpdateVmsForReservation.updateVmsForReservation(
        PROJECT_ID, ZONE, RESERVATION_NAME, newNumberOfVms);

    Assert.assertNotNull(reservation);
    Assert.assertEquals(newNumberOfVms, reservation.getSpecificReservation().getCount());
  }
}