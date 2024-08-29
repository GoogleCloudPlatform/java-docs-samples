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
import static compute.Util.getZone;

import com.google.cloud.compute.v1.DeleteReservationRequest;
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
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 10, unit = TimeUnit.MINUTES)

class CreateReservationIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String DEFAULT_ZONE = getZone();
  private static String RESERVATION_NAME;
  private static final int NUMBER_OF_VMS = 3;
  private static final String MACHINE_TYPE = "n1-standard-2";
  private static final int NUMBER_OF_ACCELERATORS = 1;
  private static final String ACCELERATOR_TYPE = "nvidia-tesla-t4";
  private static final String MIN_CPU_PLATFORM = "Intel Skylake";
  private static final int LOCAL_SSD_SIZE = 375;
  private static final String LOCAL_SSD_INTERFACE1 = "NVME";
  private static final String LOCAL_SSD_INTERFACE2 = "SCSI";
  private static final boolean SPECIFIC_RESERVATION_REQUIRED = true;

  private ByteArrayOutputStream stdOut;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    RESERVATION_NAME = "test-reserv-reg-" + UUID.randomUUID();

    stdOut.close();
    System.setOut(out);
  }

  @AfterAll
  public static void cleanup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    // Verify reservation is deleted

    try (ReservationsClient reservationsClient = ReservationsClient.create()) {

      DeleteReservationRequest deleteReservationRequest = DeleteReservationRequest.newBuilder()
          .setProject(PROJECT_ID)
          .setZone(DEFAULT_ZONE)
          .setReservation(RESERVATION_NAME)
          .build();

      Operation response = reservationsClient.deleteAsync(
          deleteReservationRequest).get(3, TimeUnit.MINUTES);

      if (response.getStatus() == Operation.Status.DONE) {
        System.out.println("Deleted reservation: " + RESERVATION_NAME);
      }
    }

    assertThat(stdOut.toString()).contains("Deleted reservation: " + RESERVATION_NAME);

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
  public void testCrateReservationWithRegionInstanceTemplate()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    CreateReservation.createReservation(PROJECT_ID, RESERVATION_NAME,
        MACHINE_TYPE, NUMBER_OF_VMS,
        DEFAULT_ZONE, NUMBER_OF_ACCELERATORS,
        ACCELERATOR_TYPE, MIN_CPU_PLATFORM,
        LOCAL_SSD_SIZE, LOCAL_SSD_INTERFACE1, LOCAL_SSD_INTERFACE2, SPECIFIC_RESERVATION_REQUIRED);

    try (ReservationsClient reservationsClient = ReservationsClient.create()) {
      Reservation reservation = reservationsClient.get(PROJECT_ID, DEFAULT_ZONE, RESERVATION_NAME);

      assertThat(stdOut.toString()).contains("Reservation created. Operation Status: DONE");
      Assert.assertEquals(RESERVATION_NAME, reservation.getName());
      Assert.assertEquals(MACHINE_TYPE,
          reservation.getSpecificReservation().getInstanceProperties().getMachineType());
      Assert.assertEquals(NUMBER_OF_VMS,
          reservation.getSpecificReservation().getCount());
      Assert.assertTrue(reservation.getZone().contains(DEFAULT_ZONE));
      Assert.assertEquals(1,
          reservation.getSpecificReservation().getInstanceProperties().getGuestAcceleratorsCount());
      Assert.assertEquals(ACCELERATOR_TYPE,
          reservation.getSpecificReservation().getInstanceProperties().getGuestAcceleratorsList()
              .get(0).getAcceleratorType());
      Assert.assertEquals(MIN_CPU_PLATFORM,
          reservation.getSpecificReservation().getInstanceProperties().getMinCpuPlatform());
      Assert.assertEquals(2,
          reservation.getSpecificReservation().getInstanceProperties().getLocalSsdsCount());
      Assert.assertEquals(LOCAL_SSD_INTERFACE1,
          reservation.getSpecificReservation().getInstanceProperties()
              .getLocalSsds(0).getInterface());
      Assert.assertEquals(SPECIFIC_RESERVATION_REQUIRED,
          reservation.getSpecificReservationRequired());
    }
  }
}