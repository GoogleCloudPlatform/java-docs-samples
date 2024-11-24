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

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Reservation;
import com.google.cloud.compute.v1.ReservationsClient;
import compute.CreateInstance;
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
@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class CreateReservationFromVmIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "us-east4-c";
  private static ReservationsClient reservationsClient;
  private static InstancesClient instancesClient;
  private static String reservationName;
  private static String instanceForReservation;
  static String javaVersion = System.getProperty("java.version").substring(0, 2);

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
    reservationsClient = ReservationsClient.create();
    instancesClient = InstancesClient.create();

    reservationName = "test-reservation-from-vm-" + javaVersion + "-"
        + UUID.randomUUID().toString().substring(0, 8);
    instanceForReservation = "test-instance-for-reserv-" + javaVersion + "-"
        + UUID.randomUUID().toString().substring(0, 8);

    CreateInstance.createInstance(PROJECT_ID, ZONE, instanceForReservation);
  }

  @AfterAll
  public static void cleanup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Delete resources created for testing.
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, instanceForReservation);

    // Clean up stale resources
    Util.cleanUpExistingReservations("test-reservation-from-vm-", PROJECT_ID, ZONE);
    Util.cleanUpExistingInstances("test-instance-for-reserv-", PROJECT_ID, ZONE);

    reservationsClient.close();
    instancesClient.close();
  }

  @Test
  public void testCreateComputeReservationFromVm()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    CreateReservationFromVm.createComputeReservationFromVm(
        PROJECT_ID, ZONE, reservationName, instanceForReservation);

    Instance instance = instancesClient.get(PROJECT_ID, ZONE, instanceForReservation);
    Reservation reservation =
        reservationsClient.get(PROJECT_ID, ZONE, reservationName);

    Assertions.assertNotNull(reservation);
    assertThat(reservation.getName()).isEqualTo(reservationName);
    Assertions.assertEquals(instance.getMinCpuPlatform(),
        reservation.getSpecificReservation().getInstanceProperties().getMinCpuPlatform());
    Assertions.assertEquals(instance.getGuestAcceleratorsList(),
        reservation.getSpecificReservation().getInstanceProperties().getGuestAcceleratorsList());

    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, reservationName);

    // Test that reservation is deleted
    Assertions.assertThrows(
        NotFoundException.class,
        () -> GetReservation.getReservation(PROJECT_ID, reservationName, ZONE));
  }
}
