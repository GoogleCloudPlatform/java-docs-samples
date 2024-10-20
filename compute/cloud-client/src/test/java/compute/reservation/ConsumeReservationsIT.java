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

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.ReservationsClient;
import compute.DeleteInstance;
import compute.Util;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 25, unit = TimeUnit.MINUTES)
public class ConsumeReservationsIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "us-central1-a";
  private static ReservationsClient reservationsClient;
  private static InstancesClient instancesClient;
  private static String RESERVATION_NAME;
  private static String RESERVATION_SHARED_NAME;
  static String javaVersion = System.getProperty("java.version").substring(0, 2);
  private static String INSTANCE_FOR_SPR;
  private static  String INSTANCE_FOR_ANY_MATCHING;
  private static  String SPECIFIC_SHARED_INSTANCE;
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

    // Initialize the clients once for all tests
    instancesClient = InstancesClient.create();
    reservationsClient = ReservationsClient.create();

    RESERVATION_SHARED_NAME = "test-reserv-shared-" + javaVersion + "-"
        + UUID.randomUUID().toString().substring(0, 8);
    RESERVATION_NAME = "test-reservaton-" + javaVersion + "-"
        + UUID.randomUUID().toString().substring(0, 8);
    INSTANCE_FOR_SPR = "test-instance-for-spr-" + javaVersion + "-"
            + UUID.randomUUID().toString().substring(0, 8);
    INSTANCE_FOR_ANY_MATCHING = "test-instance-" + javaVersion + "-"
            + UUID.randomUUID().toString().substring(0, 8);
    SPECIFIC_SHARED_INSTANCE = "test-instance-shared-" + javaVersion + "-"
            + UUID.randomUUID().toString().substring(0, 8);

    // Cleanup existing stale resources.
    Util.cleanUpExistingInstances("test-instance-for-spr-" + javaVersion, PROJECT_ID, ZONE);
    Util.cleanUpExistingInstances("test-instance-" + javaVersion, PROJECT_ID, ZONE);
    Util.cleanUpExistingInstances("test-instance-shared-" + javaVersion, PROJECT_ID, ZONE);
    Util.cleanUpExistingReservations("test-reserv-shared-" + javaVersion, PROJECT_ID, ZONE);
    Util.cleanUpExistingReservations("test-reservaton-" + javaVersion, PROJECT_ID, ZONE);
  }

  @AfterAll
  public static void cleanup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Clean up the instances
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, INSTANCE_FOR_SPR);
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, INSTANCE_FOR_ANY_MATCHING);
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, SPECIFIC_SHARED_INSTANCE);

    // Delete all reservations created for testing.
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_NAME);
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_SHARED_NAME);

    // Test that reservations are deleted
    Assertions.assertThrows(
        NotFoundException.class,
        () -> GetReservation.getReservation(PROJECT_ID, RESERVATION_NAME, ZONE));
    Assertions.assertThrows(
        NotFoundException.class,
        () -> GetReservation.getReservation(PROJECT_ID, RESERVATION_SHARED_NAME, ZONE));

    // Close the clients after all tests
    reservationsClient.close();
    instancesClient.close();
  }

  @Test
  public void testConsumeAnyMatchingReservation()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    ConsumeAnyMatchingReservation.createInstance(PROJECT_ID, ZONE, INSTANCE_FOR_ANY_MATCHING);
    TimeUnit.SECONDS.sleep(30);
    Instance instance = instancesClient.get(PROJECT_ID, ZONE, INSTANCE_FOR_ANY_MATCHING);

    Assert.assertEquals(ANY_RESERVATION.toString(),
        instance.getReservationAffinity().getConsumeReservationType());
  }

  @Test
  public void testConsumeSingleProjectReservation()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Create the reservation
    ConsumeSingleProjectReservation.createReservation(
        PROJECT_ID, RESERVATION_NAME, NUMBER_OF_VMS, ZONE);
    TimeUnit.SECONDS.sleep(30);

    ConsumeSingleProjectReservation.createInstance(
        PROJECT_ID, ZONE, INSTANCE_FOR_SPR, RESERVATION_NAME);
    TimeUnit.SECONDS.sleep(30);

    Instance instance = instancesClient.get(PROJECT_ID, ZONE, INSTANCE_FOR_SPR);

    assertThat(instance.getReservationAffinity().getValuesList())
        .contains(RESERVATION_NAME);
    Assert.assertEquals(SPECIFIC_RESERVATION.toString(),
        instance.getReservationAffinity().getConsumeReservationType());
  }

  @Test
  public void testConsumeSpecificSharedReservation()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    ConsumeSpecificSharedReservation.createReservation(PROJECT_ID,
        RESERVATION_SHARED_NAME, NUMBER_OF_VMS, ZONE);
    TimeUnit.SECONDS.sleep(30);

    Assert.assertEquals(RESERVATION_SHARED_NAME,
        reservationsClient.get(PROJECT_ID, ZONE, RESERVATION_SHARED_NAME).getName());

    ConsumeSpecificSharedReservation.createInstance(
        PROJECT_ID, ZONE, SPECIFIC_SHARED_INSTANCE, RESERVATION_SHARED_NAME);
    TimeUnit.SECONDS.sleep(30);

    // Verify that the instance was created with the correct reservation and consumeReservationType
    Instance instance = instancesClient.get(PROJECT_ID, ZONE, SPECIFIC_SHARED_INSTANCE);

    Assert.assertTrue(instance.getReservationAffinity()
        .getValuesList().get(0).contains(RESERVATION_SHARED_NAME));
    Assert.assertEquals(SPECIFIC_RESERVATION.toString(),
        instance.getReservationAffinity().getConsumeReservationType());
  }
}