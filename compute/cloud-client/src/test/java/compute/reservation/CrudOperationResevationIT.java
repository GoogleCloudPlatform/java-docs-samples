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
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Reservation;
import com.google.cloud.compute.v1.ReservationsClient;
import compute.CreateInstance;
import compute.DeleteInstance;
import compute.Util;
import java.io.IOException;
import java.util.List;
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
public class CrudOperationResevationIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "us-central1-a";
  private static String RESERVATION_NAME;
  private static String RESERVATION_NAME_FROM_VM;
  private static String INSTANCE_FOR_RESERVATION;
  private static final int NUMBER_OF_VMS = 3;
  private static ReservationsClient reservationsClient;
  private static InstancesClient instancesClient;

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
    RESERVATION_NAME = "test-reservation-" + UUID.randomUUID();
    RESERVATION_NAME_FROM_VM = "test-reservation-from-vm-" + UUID.randomUUID();
    INSTANCE_FOR_RESERVATION = "test-instance-for-reserv-" + UUID.randomUUID();

    // Cleanup existing stale resources.
    Util.cleanUpExistingReservations("test-", PROJECT_ID, ZONE);
    Util.cleanUpExistingInstances("test-instance-for-reserv-", PROJECT_ID, ZONE);

    CreateInstance.createInstance(PROJECT_ID, ZONE, INSTANCE_FOR_RESERVATION);
    CreateReservation.createReservation(
        PROJECT_ID, RESERVATION_NAME, NUMBER_OF_VMS, ZONE);
  }

  @AfterAll
  public static void cleanup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Delete resources created for testing.
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, INSTANCE_FOR_RESERVATION);
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_NAME);
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_NAME_FROM_VM);

    // Test that reservations are deleted
    Assertions.assertThrows(
        NotFoundException.class,
        () -> GetReservation.getReservation(PROJECT_ID, RESERVATION_NAME, ZONE));
    Assertions.assertThrows(
        NotFoundException.class,
        () -> GetReservation.getReservation(PROJECT_ID, RESERVATION_NAME_FROM_VM, ZONE));

    reservationsClient.close();
    instancesClient.close();
  }

  @Test
  public void testGetReservation()
      throws IOException {
    Reservation reservation = GetReservation.getReservation(
        PROJECT_ID, RESERVATION_NAME, ZONE);

    assertNotNull(reservation);
    assertThat(reservation.getName()).isEqualTo(RESERVATION_NAME);
  }

  @Test
  public void testListReservation() throws IOException {
    List<Reservation> reservations =
        ListReservations.listReservations(PROJECT_ID, ZONE);

    assertThat(reservations).isNotNull();
    Assert.assertTrue(reservations.get(0).getName().contains("test-reservation-"));
    Assert.assertTrue(reservations.get(1).getName().contains("test-reservation-"));
  }

  @Test
  public void testCreateComputeReservationFromVm()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    CreateReservationFromVm.createComputeReservationFromVm(
        PROJECT_ID, ZONE, RESERVATION_NAME_FROM_VM, INSTANCE_FOR_RESERVATION);

    Instance instance = instancesClient.get(PROJECT_ID, ZONE, INSTANCE_FOR_RESERVATION);
    Reservation reservation =
        reservationsClient.get(PROJECT_ID, ZONE, RESERVATION_NAME_FROM_VM);

    Assert.assertNotNull(reservation);
    assertThat(reservation.getName()).isEqualTo(RESERVATION_NAME_FROM_VM);
    Assert.assertEquals(instance.getMinCpuPlatform(),
        reservation.getSpecificReservation().getInstanceProperties().getMinCpuPlatform());
    Assert.assertEquals(instance.getGuestAcceleratorsList(),
        reservation.getSpecificReservation().getInstanceProperties().getGuestAcceleratorsList());
  }
}
