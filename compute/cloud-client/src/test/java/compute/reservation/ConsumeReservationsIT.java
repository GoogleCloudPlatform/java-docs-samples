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
import static org.junit.Assert.assertNotNull;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.compute.v1.AllocationSpecificSKUAllocationReservedInstanceProperties;
import com.google.cloud.compute.v1.AllocationSpecificSKUReservation;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.Reservation;
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
@Timeout(value = 6, unit = TimeUnit.MINUTES)
public class ConsumeReservationsIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "us-central1-a";
  static String templateUUID = UUID.randomUUID().toString();
  private static final String  RESERVATION_NAME = "test-reservaton-" + templateUUID;
  private static final String INSTANCE_FOR_SPR = "test-instance-for-spr-" + templateUUID;
  private static final String INSTANCE_FOR_ANY_MATCHING = "test-instance-" + templateUUID;
  private static final String SPECIFIC_SHARED_INSTANCE = "test-instance-shared-" + templateUUID;
  private static final String MACHINE_TYPE = "n1-standard-4";
  private static final String SOURCE_IMAGE = "projects/debian-cloud/global/images/family/debian-11";
  private static final String NETWORK_NAME = "default";
  private static final long DISK_SIZE_GB = 10L;
  private static final String MIN_CPU_PLATFORM = "Intel Skylake";

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

    ConsumeReservationsIT.createReservation(
        PROJECT_ID, RESERVATION_NAME, ZONE);
  }

  @AfterAll
  public static void cleanup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Delete all instances created for testing.
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, INSTANCE_FOR_SPR);
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, INSTANCE_FOR_ANY_MATCHING);
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, SPECIFIC_SHARED_INSTANCE);

    // Delete all reservations created for testing.
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_NAME);

    // Test that reservation is deleted
    Assertions.assertThrows(
        NotFoundException.class,
        () -> GetReservation.getReservation(PROJECT_ID, RESERVATION_NAME, ZONE));

    // Clean up stale resources
    Util.cleanUpExistingInstances("test-instance-", PROJECT_ID, ZONE);
    Util.cleanUpExistingReservations("test-reservation-", PROJECT_ID, ZONE);
  }

  @Test
  public void testConsumeAnyMatchingReservation()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Instance instance = ConsumeAnyMatchingReservation
        .createInstanceAsync(PROJECT_ID, ZONE, INSTANCE_FOR_ANY_MATCHING,
        MACHINE_TYPE, SOURCE_IMAGE, DISK_SIZE_GB, NETWORK_NAME, MIN_CPU_PLATFORM);

    assertNotNull(instance);
    Assert.assertEquals(ANY_RESERVATION.toString(),
        instance.getReservationAffinity().getConsumeReservationType());
  }

  @Test
  public void testConsumeSingleProjectReservation()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Instance instance = ConsumeSingleProjectReservation.createInstanceAsync(
        PROJECT_ID, ZONE, INSTANCE_FOR_SPR, RESERVATION_NAME, MACHINE_TYPE,
        SOURCE_IMAGE, DISK_SIZE_GB, NETWORK_NAME, MIN_CPU_PLATFORM);

    assertNotNull(instance);
    assertThat(instance.getReservationAffinity().getValuesList())
        .contains(RESERVATION_NAME);
    Assert.assertEquals(SPECIFIC_RESERVATION.toString(),
        instance.getReservationAffinity().getConsumeReservationType());
  }

  @Test
  public void testConsumeSpecificSharedReservation()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Instance instance = ConsumeSpecificSharedReservation.createInstanceAsync(
        PROJECT_ID, ZONE, SPECIFIC_SHARED_INSTANCE, RESERVATION_NAME, MACHINE_TYPE,
        SOURCE_IMAGE, DISK_SIZE_GB, NETWORK_NAME, MIN_CPU_PLATFORM);

    assertNotNull(instance);
    Assert.assertTrue(instance.getReservationAffinity()
        .getValuesList().get(0).contains(RESERVATION_NAME));
    Assert.assertEquals(SPECIFIC_RESERVATION.toString(),
        instance.getReservationAffinity().getConsumeReservationType());
  }

  // Creates reservation with the given parameters.
  public static void createReservation(
      String projectId, String reservationName, String zone)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    boolean specificReservationRequired = true;
    int numberOfVms = 3;
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ReservationsClient reservationsClient = ReservationsClient.create()) {
      Reservation reservation =
          Reservation.newBuilder()
              .setName(reservationName)
              .setZone(zone)
              .setSpecificReservationRequired(specificReservationRequired)
              .setSpecificReservation(
                  AllocationSpecificSKUReservation.newBuilder()
                      .setCount(numberOfVms)
                      .setInstanceProperties(
                          AllocationSpecificSKUAllocationReservedInstanceProperties.newBuilder()
                              .setMachineType(MACHINE_TYPE)
                              .setMinCpuPlatform(MIN_CPU_PLATFORM)
                              .build())
                      .build())
              .build();

      reservationsClient.insertAsync(projectId, zone, reservation).get(3, TimeUnit.MINUTES);
    }
  }
}