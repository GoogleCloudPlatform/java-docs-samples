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

import static com.google.cloud.compute.v1.ReservationAffinity.ConsumeReservationType.ANY_RESERVATION;
import static com.google.cloud.compute.v1.ReservationAffinity.ConsumeReservationType.SPECIFIC_RESERVATION;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static compute.Util.getZone;
import static org.junit.Assert.assertNotNull;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
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

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = getZone();
  private static String RESERVATION_NAME;
  private static String INSTANCE_FOR_SPR;
  private static String INSTANCE_NAME;
  private static String SHARED_RESERVATION_NAME;
  private static final int NUMBER_OF_VMS = 3;
  private static String MACHINE_TYPE;
  private static String MIN_CPU_PLATFORM;
  private static InstancesClient instancesClient;


  private ByteArrayOutputStream stdOut;

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
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    // Initialize the client once for all tests
    instancesClient = InstancesClient.create();

    RESERVATION_NAME = "test-reserv-" + UUID.randomUUID();
    SHARED_RESERVATION_NAME = "test-shared-reserv-" + UUID.randomUUID();
    //Instance for Single Project Reservation consuming
    INSTANCE_FOR_SPR = "test-instance-for-spr-" + UUID.randomUUID().toString().substring(0, 8);
    INSTANCE_NAME = "test-instance-" + UUID.randomUUID().toString().substring(0, 8);
    MACHINE_TYPE = "n2-standard-32";
    MIN_CPU_PLATFORM = "Intel Cascade Lake";

    // Create the reservation
    ConsumeSingleProjectReservation.createReservation(
        PROJECT_ID, SHARED_RESERVATION_NAME, NUMBER_OF_VMS,
        ZONE, MACHINE_TYPE, MIN_CPU_PLATFORM, true);
    assertThat(stdOut.toString()).contains("Reservation created. Operation Status: DONE");
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

  @AfterAll
  public static void cleanup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    // Clean up the instances
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, INSTANCE_FOR_SPR);
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, INSTANCE_NAME);

    // Verify reservation is deleted
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_NAME);
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, SHARED_RESERVATION_NAME);

    assertThat(stdOut.toString()).contains("Deleted reservation: " + RESERVATION_NAME);
    assertThat(stdOut.toString()).contains("Deleted reservation: " + SHARED_RESERVATION_NAME);
    try (ReservationsClient reservationsClient = ReservationsClient.create()) {
      // Get the reservation.
      Assertions.assertThrows(
          NotFoundException.class,
          () -> reservationsClient.get(PROJECT_ID, ZONE, RESERVATION_NAME));
    }

    stdOut.close();
    System.setOut(out);
  }

  @Test
  public void testCreateReservation()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    CreateReservation.createReservation(
        PROJECT_ID, RESERVATION_NAME, NUMBER_OF_VMS, ZONE);

    try (ReservationsClient reservationsClient = ReservationsClient.create()) {
      Reservation reservation = reservationsClient.get(PROJECT_ID, ZONE, RESERVATION_NAME);

      assertThat(stdOut.toString()).contains("Reservation created. Operation Status: DONE");
      Assert.assertEquals(RESERVATION_NAME, reservation.getName());
      Assert.assertEquals(NUMBER_OF_VMS,
          reservation.getSpecificReservation().getCount());
      Assert.assertTrue(reservation.getZone().contains(ZONE));
    }
  }

  @Test
  public void testConsumeAnyMatchingReservation()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    ConsumeAnyMatchingReservation.createInstance(
        PROJECT_ID, ZONE, INSTANCE_NAME, MACHINE_TYPE, MIN_CPU_PLATFORM);

    String output = stdOut.toString();
    assertThat(output).contains("Operation Status: DONE");

    Instance instance = instancesClient.get(PROJECT_ID, ZONE, INSTANCE_NAME);
    assertNotNull(instance);
    Assert.assertEquals(ANY_RESERVATION.toString(),
        instance.getReservationAffinity().getConsumeReservationType());
  }

  @Test
  public void testConsumeSingleProjectReservation()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    ConsumeSingleProjectReservation.createInstance(
        PROJECT_ID, ZONE, INSTANCE_FOR_SPR, MACHINE_TYPE,
        MIN_CPU_PLATFORM, SHARED_RESERVATION_NAME);

    String output = stdOut.toString();
    assertThat(output).contains("Operation Status: DONE");

    // Verify that the instance was created in the correct reservation
    Instance instance = instancesClient.get(PROJECT_ID, ZONE, INSTANCE_FOR_SPR);
    assertNotNull(instance);
    assertThat(instance.getReservationAffinity().getValuesList())
        .contains(SHARED_RESERVATION_NAME);
    Assert.assertEquals(SPECIFIC_RESERVATION.toString(),
        instance.getReservationAffinity().getConsumeReservationType());
  }
}