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
import static compute.Util.getZone;

import com.google.cloud.compute.v1.AllocationSpecificSKUAllocationReservedInstanceProperties;
import com.google.cloud.compute.v1.AllocationSpecificSKUReservation;
import com.google.cloud.compute.v1.InsertReservationRequest;
import com.google.cloud.compute.v1.InstanceTemplatesClient;
import org.mockito.Mockito;
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

import compute.CreateInstanceTemplate;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReservationIT {

  private static String PROJECT_ID;
  private static String ZONE;
  private static String RESERVATION_NAME;
  private static String[] CONSUMER_PROJECT_IDS;
  private static String INSTANCE_TEMPLATE_URI;

  private ByteArrayOutputStream stdOut;

  @BeforeAll
  public static void setUp()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
    ZONE = getZone();
    RESERVATION_NAME = "test-reservation-" + UUID.randomUUID();
    CONSUMER_PROJECT_IDS = new String[]{PROJECT_ID, "CONSUMER_PROJECT_ID_1", "CONSUMER_PROJECT_ID_2"};
    String instanceTemplateName = "test-inst-for-shared-res" + UUID.randomUUID();
        CreateInstanceTemplate.createInstanceTemplate(PROJECT_ID,  instanceTemplateName);
    try (InstanceTemplatesClient instanceTemplatesClient = InstanceTemplatesClient.create()) {
       Assert.assertTrue(instanceTemplatesClient.get(PROJECT_ID,instanceTemplateName).getName().contains(instanceTemplateName));}
    INSTANCE_TEMPLATE_URI = String.format("projects/%s/global/instanceTemplates/%s",PROJECT_ID, instanceTemplateName);
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
  public void testCreateSharedReservation()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    CreateSharedReservation.createSharedReservation(
        CONSUMER_PROJECT_IDS, ZONE, RESERVATION_NAME,
        INSTANCE_TEMPLATE_URI, 3);

    assertThat(stdOut.toString()).contains("Reservation created. Operation Status: DONE");
  }
}