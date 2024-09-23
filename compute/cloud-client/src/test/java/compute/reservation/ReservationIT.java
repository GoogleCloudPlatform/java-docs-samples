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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.compute.v1.AllocationSpecificSKUReservation;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.DeleteRegionInstanceTemplateRequest;
import com.google.cloud.compute.v1.InsertRegionInstanceTemplateRequest;
import com.google.cloud.compute.v1.InstanceProperties;
import com.google.cloud.compute.v1.InstanceTemplate;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionInstanceTemplatesClient;
import com.google.cloud.compute.v1.Reservation;
import com.google.cloud.compute.v1.ReservationsClient;
import com.google.cloud.compute.v1.ShareSettings;
import com.google.cloud.compute.v1.ShareSettingsProjectConfig;
import compute.CreateInstanceTemplate;
import compute.DeleteInstanceTemplate;
import compute.GetInstanceTemplate;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import compute.Util;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

@RunWith(JUnit4.class)
@Timeout(value = 10, unit = TimeUnit.MINUTES)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ReservationIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = getZone();
  private static final String REGION = ZONE.substring(0, ZONE.lastIndexOf('-'));
  private static ReservationsClient reservationsClient;
  private static String RESERVATION_NAME;
  private static String RESERVATION_NAME_GLOBAL;
  private static String RESERVATION_NAME_REGIONAL;
  private static String RESERVATION_NAME_SHARED;
  private static String GLOBAL_INSTANCE_TEMPLATE_URI;
  private static String REGIONAL_INSTANCE_TEMPLATE_URI;
  private static String INSTANCE_TEMPLATE_URI_SHARED_RESERV;
  private static final String GLOBAL_INSTANCE_TEMPLATE_NAME =
      "test-global-instance-" + UUID.randomUUID();
  private static final String REGIONAL_INSTANCE_TEMPLATE_NAME =
      "test-regional-instance-" + UUID.randomUUID();
  private static final String INSTANCE_TEMPLATE_NAME_SHARED_RESERV =
      "test-inst-for-shared-res-" + UUID.randomUUID();
  private static final int NUMBER_OF_VMS = 3;

  private ByteArrayOutputStream stdOut;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setUp()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    // Cleanup existing stale resources.
    Util.cleanUpExistingInstances("test-global-instance", PROJECT_ID, ZONE);
    Util.cleanUpExistingInstances("test-regional-instance", PROJECT_ID, ZONE);
    Util.cleanUpExistingInstances("test-inst-for-shared-res", PROJECT_ID, ZONE);

    // Initialize the client once for all tests
    reservationsClient = ReservationsClient.create();

    RESERVATION_NAME = "test-reserv-" + UUID.randomUUID();
    RESERVATION_NAME_GLOBAL = "test-reserv-global-" + UUID.randomUUID();
    RESERVATION_NAME_REGIONAL = "test-reserv-regional-" + UUID.randomUUID();
    RESERVATION_NAME_SHARED = "test-reserv-shared-" + UUID.randomUUID();
    GLOBAL_INSTANCE_TEMPLATE_URI = String.format("projects/%s/global/instanceTemplates/%s",
        PROJECT_ID, GLOBAL_INSTANCE_TEMPLATE_NAME);
    REGIONAL_INSTANCE_TEMPLATE_URI =
        String.format("projects/%s/regions/%s/instanceTemplates/%s",
            PROJECT_ID, REGION, REGIONAL_INSTANCE_TEMPLATE_NAME);
    INSTANCE_TEMPLATE_URI_SHARED_RESERV = String.format("projects/%s/global/instanceTemplates/%s",
        PROJECT_ID, INSTANCE_TEMPLATE_NAME_SHARED_RESERV);

    // Create instance template for shares reservation.
    CreateInstanceTemplate.createInstanceTemplate(
        PROJECT_ID, INSTANCE_TEMPLATE_NAME_SHARED_RESERV);
    assertThat(stdOut.toString())
        .contains("Instance Template Operation Status " + INSTANCE_TEMPLATE_NAME_SHARED_RESERV);
    // Create instance template with GLOBAL location.
    CreateInstanceTemplate.createInstanceTemplate(PROJECT_ID, GLOBAL_INSTANCE_TEMPLATE_NAME);
    assertThat(stdOut.toString())
        .contains("Instance Template Operation Status " + GLOBAL_INSTANCE_TEMPLATE_NAME);
    // Create instance template with REGIONAL location.
    ReservationIT.createRegionalInstanceTemplate(
        PROJECT_ID, REGIONAL_INSTANCE_TEMPLATE_NAME, ZONE);
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
    Assertions.assertThrows(
        NotFoundException.class,
        () -> GetInstanceTemplate.getInstanceTemplate(PROJECT_ID, GLOBAL_INSTANCE_TEMPLATE_NAME));

    // Delete instance template with REGIONAL location.
    ReservationIT.deleteRegionalInstanceTemplate(
        PROJECT_ID, ZONE, REGIONAL_INSTANCE_TEMPLATE_NAME);
    Assertions.assertThrows(
        NotFoundException.class,
        () -> GetInstanceTemplate.getInstanceTemplate(PROJECT_ID, REGIONAL_INSTANCE_TEMPLATE_NAME));

    // Delete instance template for shared reservation
    DeleteInstanceTemplate.deleteInstanceTemplate(PROJECT_ID, INSTANCE_TEMPLATE_NAME_SHARED_RESERV);
    Assertions.assertThrows(
        NotFoundException.class,
        () -> GetInstanceTemplate
            .getInstanceTemplate(PROJECT_ID, INSTANCE_TEMPLATE_NAME_SHARED_RESERV));

    // Delete all reservations created for testing.
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_NAME);
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_NAME_GLOBAL);
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_NAME_REGIONAL);

    // Test that reservations are deleted
    Assertions.assertThrows(
        NotFoundException.class,
        () -> GetReservation.getReservation(PROJECT_ID, RESERVATION_NAME, ZONE));
    Assertions.assertThrows(
        NotFoundException.class,
        () -> GetReservation.getReservation(PROJECT_ID, RESERVATION_NAME_GLOBAL, ZONE));
    Assertions.assertThrows(
        NotFoundException.class,
        () -> GetReservation.getReservation(PROJECT_ID, RESERVATION_NAME_REGIONAL, ZONE));

    // Close the client after all tests
    reservationsClient.close();

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
  public void firstCreateReservationTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    CreateReservation.createReservation(
        PROJECT_ID, RESERVATION_NAME, NUMBER_OF_VMS, ZONE);

    Reservation reservation = reservationsClient.get(PROJECT_ID, ZONE, RESERVATION_NAME);

    assertThat(stdOut.toString()).contains("Reservation created. Operation Status: DONE");
    Assert.assertEquals(RESERVATION_NAME, reservation.getName());
    Assert.assertEquals(NUMBER_OF_VMS,
        reservation.getSpecificReservation().getCount());
    Assert.assertTrue(reservation.getZone().contains(ZONE));
  }

  @Test
  public void secondGetReservationTest()
      throws IOException {
    Reservation reservation = GetReservation.getReservation(
        PROJECT_ID, RESERVATION_NAME, ZONE);

    assertNotNull(reservation);
    assertThat(reservation.getName()).isEqualTo(RESERVATION_NAME);
  }

  @Test
  public void thirdListReservationTest() throws IOException {
    List<Reservation> reservations =
        ListReservations.listReservations(PROJECT_ID, ZONE);

    assertThat(reservations).isNotNull();
    Assert.assertTrue(reservations.get(0).getName().contains("test-reserv"));
    Assert.assertTrue(reservations.get(1).getName().contains("test-reserv"));
    Assert.assertTrue(reservations.get(2).getName().contains("test-reserv"));
  }

  @Test
  public void firstCreateReservationWithGlobalInstanceTemplateTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    CreateReservationForInstanceTemplate.createReservationForInstanceTemplate(
        PROJECT_ID, RESERVATION_NAME_GLOBAL,
        GLOBAL_INSTANCE_TEMPLATE_URI, NUMBER_OF_VMS, ZONE);

    Reservation reservation = reservationsClient.get(PROJECT_ID, ZONE, RESERVATION_NAME_GLOBAL);

    assertThat(stdOut.toString()).contains("Reservation created. Operation Status: DONE");
    Assert.assertTrue(reservation.getSpecificReservation()
        .getSourceInstanceTemplate().contains(GLOBAL_INSTANCE_TEMPLATE_NAME));
    Assert.assertEquals(RESERVATION_NAME_GLOBAL, reservation.getName());
  }

  @Test
  public void firstCreateReservationWithRegionInstanceTemplateTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    CreateReservationForInstanceTemplate.createReservationForInstanceTemplate(
        PROJECT_ID, RESERVATION_NAME_REGIONAL, REGIONAL_INSTANCE_TEMPLATE_URI,
        NUMBER_OF_VMS, ZONE);
    Reservation reservation = reservationsClient.get(PROJECT_ID, ZONE, RESERVATION_NAME_REGIONAL);
    assertThat(stdOut.toString()).contains("Reservation created. Operation Status: DONE");
    Assert.assertTrue(reservation.getSpecificReservation()
        .getSourceInstanceTemplate().contains(REGIONAL_INSTANCE_TEMPLATE_NAME));
    Assert.assertTrue(reservation.getZone().contains(ZONE));
    Assert.assertEquals(RESERVATION_NAME_REGIONAL, reservation.getName());
  }

  @Test
  public void testCreateSharedReservation()
      throws ExecutionException, InterruptedException, TimeoutException {

    // Mock the ReservationsClient
    ReservationsClient mockReservationsClient = mock(ReservationsClient.class);

    ShareSettings shareSettings = ShareSettings.newBuilder()
        .setShareType(String.valueOf(ShareSettings.ShareType.SPECIFIC_PROJECTS))
        .putProjectMap("CONSUMER_PROJECT_ID_1", ShareSettingsProjectConfig.newBuilder().build())
        .putProjectMap("CONSUMER_PROJECT_ID_2", ShareSettingsProjectConfig.newBuilder().build())
        .build();

    Reservation reservation =
        Reservation.newBuilder()
            .setName(RESERVATION_NAME_SHARED)
            .setZone(ZONE)
            .setSpecificReservationRequired(true)
            .setShareSettings(shareSettings)
            .setSpecificReservation(
                AllocationSpecificSKUReservation.newBuilder()
                    .setCount(NUMBER_OF_VMS)
                    .setSourceInstanceTemplate(INSTANCE_TEMPLATE_URI_SHARED_RESERV)
                    .build())
            .build();

    OperationFuture mockFuture = mock(OperationFuture.class);
    when(mockReservationsClient.insertAsync(PROJECT_ID, ZONE, reservation))
        .thenReturn(mockFuture);

    // Mock the Operation
    Operation mockOperation = mock(Operation.class);
    when(mockFuture.get(3, TimeUnit.MINUTES)).thenReturn(mockOperation);
    when(mockOperation.hasError()).thenReturn(false);
    when(mockOperation.getStatus()).thenReturn(Operation.Status.DONE);

    // Create an instance, passing in the mock client
    CreateSharedReservation creator = new CreateSharedReservation(mockReservationsClient);

    creator.createSharedReservation(PROJECT_ID, ZONE,
        RESERVATION_NAME_SHARED, INSTANCE_TEMPLATE_URI_SHARED_RESERV, NUMBER_OF_VMS);

    verify(mockReservationsClient, times(1))
        .insertAsync(PROJECT_ID, ZONE, reservation);
    assertThat(stdOut.toString()).contains("Reservation created. Operation Status: DONE");
  }

  // Creates a new instance template with the REGIONAL location.
  public static void createRegionalInstanceTemplate(
      String projectId, String templateName, String zone)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (RegionInstanceTemplatesClient templatesClientRegion =
             RegionInstanceTemplatesClient.create()) {

      String machineType = "n1-standard-1"; // Example machine type
      String sourceImage = "projects/debian-cloud/global/images/family/debian-11"; // Example image
      String region = zone.substring(0, zone.lastIndexOf('-')); // Extract the region from the zone

      // Define the boot disk for the instance template
      AttachedDisk attachedDisk = AttachedDisk.newBuilder()
          .setInitializeParams(AttachedDiskInitializeParams.newBuilder()
              .setSourceImage(sourceImage)
              .setDiskType("pd-balanced") // Example disk type
              .setDiskSizeGb(100L) // Example disk size
              .build())
          .setAutoDelete(true)
          .setBoot(true)
          .build();

      // Define the network interface for the instance template
      // Note: The subnetwork must be in the same region as the instance template.
      NetworkInterface networkInterface = NetworkInterface.newBuilder()
          .setName("my-network-test")
          .setSubnetwork(String.format("projects/%s/regions/%s/subnetworks/default",
              PROJECT_ID, region))
          .build();

      // Define the instance properties for the template
      InstanceProperties instanceProperties = InstanceProperties.newBuilder()
          .addDisks(attachedDisk)
          .setMachineType(machineType)
          .addNetworkInterfaces(networkInterface)
          .build();

      // Build the instance template object
      InstanceTemplate instanceTemplate = InstanceTemplate.newBuilder()
          .setName(templateName)
          .setProperties(instanceProperties)
          .build();

      // Create the request to insert the instance template
      InsertRegionInstanceTemplateRequest insertInstanceTemplateRequest =
          InsertRegionInstanceTemplateRequest
              .newBuilder()
              .setProject(projectId)
              .setRegion(region)
              .setInstanceTemplateResource(instanceTemplate)
              .build();

      // Send the request and wait for the operation to complete
      Operation response = templatesClientRegion.insertAsync(insertInstanceTemplateRequest)
          .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Instance Template creation failed! " + response);
        return;
      }
      System.out.printf("Instance Template Operation Status: %s%n", response.getStatus());
    }
  }

  // Delete an instance template with the REGIONAL location.
  private static void deleteRegionalInstanceTemplate(
      String projectId, String zone, String templateName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (RegionInstanceTemplatesClient regionInstanceTemplatesClient =
             RegionInstanceTemplatesClient.create()) {
      String region = zone.substring(0, zone.lastIndexOf('-')); // Extract the region from the zone

      DeleteRegionInstanceTemplateRequest deleteInstanceTemplateRequest =
          DeleteRegionInstanceTemplateRequest
              .newBuilder()
              .setProject(projectId)
              .setRegion(region)
              .setInstanceTemplate(templateName)
              .build();

      Operation response = regionInstanceTemplatesClient.deleteAsync(
          deleteInstanceTemplateRequest).get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Instance template deletion failed ! ! " + response);
        return;
      }
      System.out.printf("Instance template deletion operation status for %s: %s ", templateName,
          response.getStatus());
    }
  }
}