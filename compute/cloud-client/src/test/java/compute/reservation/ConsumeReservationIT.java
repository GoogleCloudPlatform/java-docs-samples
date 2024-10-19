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

import static com.google.cloud.compute.v1.ReservationAffinity.ConsumeReservationType.SPECIFIC_RESERVATION;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 25, unit = TimeUnit.MINUTES)
public class ConsumeReservationIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "asia-south1-a";
  static String javaVersion = System.getProperty("java.version").substring(0, 2);
  private static ReservationsClient reservationsClient;
  private static InstancesClient instancesClient;
  private static final String RESERVATION_SHARED_NAME = "test-reservation-shared-" + javaVersion
      + "-" + UUID.randomUUID().toString().substring(0, 8);
  private static final String SPECIFIC_SHARED_INSTANCE_NAME =
      "test-shared-instance-" + javaVersion  + "-"
          + UUID.randomUUID().toString().substring(0, 8);
  private static final int NUMBER_OF_VMS = 3;
  private static final String MACHINE_TYPE = "n2-standard-32";
  private static final String MIN_CPU_PLATFORM = "Intel Cascade Lake";

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

    // Cleanup existing stale resources.
    Util.cleanUpExistingInstances("test-shared-instance-" + javaVersion, PROJECT_ID, ZONE);
    Util.cleanUpExistingReservations("test-shared-instance-" + javaVersion, PROJECT_ID, ZONE);

    // Initialize the clients once for all tests
    reservationsClient = ReservationsClient.create();
    instancesClient = InstancesClient.create();
  }

  @AfterAll
  public static void cleanup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Delete resources created for testing.
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, SPECIFIC_SHARED_INSTANCE_NAME);
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_SHARED_NAME);

    // Test that reservations are deleted
    Assertions.assertThrows(
        NotFoundException.class,
        () -> GetReservation.getReservation(PROJECT_ID, RESERVATION_SHARED_NAME, ZONE));

    // Close clients after all tests
    reservationsClient.close();
    instancesClient.close();
  }

  @Test
  public void testConsumeSpecificSharedReservation()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    ConsumeSpecificSharedReservation.createReservation(PROJECT_ID,
        RESERVATION_SHARED_NAME, NUMBER_OF_VMS, ZONE,
        MACHINE_TYPE, MIN_CPU_PLATFORM, true);

    Assertions.assertEquals(RESERVATION_SHARED_NAME,
        reservationsClient.get(PROJECT_ID, ZONE, RESERVATION_SHARED_NAME).getName());

    ConsumeSpecificSharedReservation.createInstance(
        PROJECT_ID, ZONE, SPECIFIC_SHARED_INSTANCE_NAME, MACHINE_TYPE,
        MIN_CPU_PLATFORM, RESERVATION_SHARED_NAME);

    // Verify that the instance was created with the correct reservation and consumeReservationType
    Instance instance = instancesClient.get(PROJECT_ID, ZONE, SPECIFIC_SHARED_INSTANCE_NAME);

    Assertions.assertTrue(instance.getReservationAffinity()
        .getValuesList().get(0).contains(RESERVATION_SHARED_NAME));
    Assertions.assertEquals(SPECIFIC_RESERVATION.toString(),
        instance.getReservationAffinity().getConsumeReservationType());
  }
}
