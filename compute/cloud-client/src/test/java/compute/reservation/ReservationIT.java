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
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Reservation;
import com.google.cloud.compute.v1.ReservationsClient;
import compute.CreateInstanceTemplate;
import compute.CreateRegionalInstanceTemplate;
import compute.DeleteInstance;
import compute.DeleteInstanceTemplate;
import compute.DeleteRegionalInstanceTemplate;
import compute.Util;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
public class ReservationIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "us-west1-a";
  private static final String REGION = ZONE.substring(0, ZONE.lastIndexOf('-'));
  static String javaVersion = System.getProperty("java.version").substring(0, 2);
  private static ReservationsClient reservationsClient;
  private static InstancesClient instancesClient;
  private static String RESERVATION_NAME_GLOBAL;
  private static String RESERVATION_NAME_REGIONAL;
  private static String GLOBAL_INSTANCE_TEMPLATE_URI;
  private static String REGIONAL_INSTANCE_TEMPLATE_URI;
  private static String RESERVATION_SHARED_NAME;
  private static String SPECIFIC_SHARED_INSTANCE_NAME;
  private static  String GLOBAL_INSTANCE_TEMPLATE_NAME;
  private static String REGIONAL_INSTANCE_TEMPLATE_NAME;
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
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    // Cleanup existing stale resources.
    Util.cleanUpExistingInstances("test-shared-instance-" + javaVersion, PROJECT_ID, ZONE);
    Util.cleanUpExistingInstanceTemplates("test-global-inst-temp-" + javaVersion, PROJECT_ID);
    Util.cleanUpExistingRegionalInstanceTemplates(
        "test-regional-inst-temp-" + javaVersion, PROJECT_ID, ZONE);
    Util.cleanUpExistingReservations(
        "test-reservation-global-" + javaVersion, PROJECT_ID, ZONE);
    Util.cleanUpExistingReservations("test-reservation-regional-" + javaVersion, PROJECT_ID, ZONE);
    Util.cleanUpExistingReservations("test-shared-instance-" + javaVersion, PROJECT_ID, ZONE);

    // Initialize the client once for all tests
    reservationsClient = ReservationsClient.create();
    instancesClient = InstancesClient.create();

    RESERVATION_NAME_GLOBAL = "test-reservation-global-" + javaVersion  + "-"
        + UUID.randomUUID().toString().substring(0, 8);
    RESERVATION_NAME_REGIONAL = "test-reservation-regional-" + javaVersion  + "-"
        + UUID.randomUUID().toString().substring(0, 8);
    GLOBAL_INSTANCE_TEMPLATE_NAME =
        "test-global-inst-temp-" + javaVersion + "-" + UUID.randomUUID().toString().substring(0, 8);
    REGIONAL_INSTANCE_TEMPLATE_NAME =
        "test-regional-inst-temp-" + javaVersion  + "-"
            + UUID.randomUUID().toString().substring(0, 8);
    GLOBAL_INSTANCE_TEMPLATE_URI = String.format("projects/%s/global/instanceTemplates/%s",
        PROJECT_ID, GLOBAL_INSTANCE_TEMPLATE_NAME);
    REGIONAL_INSTANCE_TEMPLATE_URI =
        String.format("projects/%s/regions/%s/instanceTemplates/%s",
            PROJECT_ID, REGION, REGIONAL_INSTANCE_TEMPLATE_NAME);
    RESERVATION_SHARED_NAME = "test-reservation-shared-" + javaVersion
        + "-" + UUID.randomUUID().toString().substring(0, 8);
    SPECIFIC_SHARED_INSTANCE_NAME =
        "test-shared-instance-" + javaVersion  + "-"
            + UUID.randomUUID().toString().substring(0, 8);

    // Create instance template with GLOBAL location.
    CreateInstanceTemplate.createInstanceTemplate(PROJECT_ID, GLOBAL_INSTANCE_TEMPLATE_NAME);
    assertThat(stdOut.toString())
        .contains("Instance Template Operation Status " + GLOBAL_INSTANCE_TEMPLATE_NAME);
    // Create instance template with REGIONAL location.
    CreateRegionalInstanceTemplate.createRegionalInstanceTemplate(
        PROJECT_ID, REGION, REGIONAL_INSTANCE_TEMPLATE_NAME);
    assertThat(stdOut.toString()).contains("Instance Template Operation Status: DONE");

    stdOut.close();
    System.setOut(out);
  }

  @AfterAll
  public static void cleanup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    // Delete instance template with GLOBAL location.
    DeleteInstanceTemplate.deleteInstanceTemplate(PROJECT_ID, GLOBAL_INSTANCE_TEMPLATE_NAME);
    assertThat(stdOut.toString())
        .contains("Instance template deletion operation status for "
            + GLOBAL_INSTANCE_TEMPLATE_NAME);

    // Delete instance template with REGIONAL location.
    DeleteRegionalInstanceTemplate.deleteRegionalInstanceTemplate(
        PROJECT_ID, REGION, REGIONAL_INSTANCE_TEMPLATE_NAME);
    assertThat(stdOut.toString())
        .contains("Instance template deletion operation status for "
            + REGIONAL_INSTANCE_TEMPLATE_NAME);

    // Delete all reservations created for testing.
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, SPECIFIC_SHARED_INSTANCE_NAME);
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_NAME_GLOBAL);
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_NAME_REGIONAL);
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_SHARED_NAME);

    // Test that reservations are deleted
    Assertions.assertThrows(
        NotFoundException.class,
        () -> GetReservation.getReservation(PROJECT_ID, RESERVATION_NAME_GLOBAL, ZONE));
    Assertions.assertThrows(
        NotFoundException.class,
        () -> GetReservation.getReservation(PROJECT_ID, RESERVATION_NAME_REGIONAL, ZONE));
    Assertions.assertThrows(
        NotFoundException.class,
        () -> GetReservation.getReservation(PROJECT_ID, RESERVATION_SHARED_NAME, ZONE));

    // Close the client after all tests
    reservationsClient.close();
    instancesClient.close();

    stdOut.close();
    System.setOut(out);
  }

  @Test
  public void testCreateReservationWithGlobalInstanceTemplate()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    CreateReservationForInstanceTemplate.createReservationForInstanceTemplate(
        PROJECT_ID, RESERVATION_NAME_GLOBAL,
        GLOBAL_INSTANCE_TEMPLATE_URI, NUMBER_OF_VMS, ZONE);
    Reservation reservation = reservationsClient.get(PROJECT_ID, ZONE, RESERVATION_NAME_GLOBAL);

    Assert.assertTrue(reservation.getSpecificReservation()
        .getSourceInstanceTemplate().contains(GLOBAL_INSTANCE_TEMPLATE_NAME));
    Assert.assertEquals(RESERVATION_NAME_GLOBAL, reservation.getName());
  }

  @Test
  public void testCreateReservationWithRegionInstanceTemplate()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    CreateReservationForInstanceTemplate.createReservationForInstanceTemplate(
        PROJECT_ID, RESERVATION_NAME_REGIONAL, REGIONAL_INSTANCE_TEMPLATE_URI,
        NUMBER_OF_VMS, ZONE);
    Reservation reservation = reservationsClient.get(PROJECT_ID, ZONE, RESERVATION_NAME_REGIONAL);

    Assert.assertTrue(reservation.getSpecificReservation()
        .getSourceInstanceTemplate().contains(REGIONAL_INSTANCE_TEMPLATE_NAME));
    Assert.assertTrue(reservation.getZone().contains(ZONE));
    Assert.assertEquals(RESERVATION_NAME_REGIONAL, reservation.getName());
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