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

import com.google.cloud.compute.v1.InstanceTemplatesClient;
import com.google.cloud.compute.v1.Reservation;
import com.google.cloud.compute.v1.ReservationsClient;
import compute.CreateInstanceTemplate;
import compute.DeleteInstanceTemplate;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CreateReservationTest {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "us-central1-a";
  private static final String RESERVATION_NAME =
      "test-reservation-" + UUID.randomUUID().toString().substring(0, 8);
  private static final String INSTANCE_TEMPLATE_NAME =
      "test-instance-template-" + UUID.randomUUID().toString().substring(0, 8);
  private static final long NUMBER_OF_VMS = 3;

  private ByteArrayOutputStream stdOut;

  // Check if the instance template exists.
  public static boolean checkInstanceTemplateExists(String project, String instanceTemplate)
      throws IOException {
    try (InstanceTemplatesClient instanceTemplatesClient = InstanceTemplatesClient.create()) {
      instanceTemplatesClient.get(project, instanceTemplate);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  // Create an instance template for the test.
  @BeforeClass
  public static void setUp()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    if (!checkInstanceTemplateExists(PROJECT_ID, INSTANCE_TEMPLATE_NAME)) {
      CreateInstanceTemplate.createInstanceTemplate(PROJECT_ID, INSTANCE_TEMPLATE_NAME);
    }
  }

  @Before
  public void beforeTest() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @After
  public void tearDown()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final PrintStream out = System.out;
    // Delete the instanceTemplate.
    DeleteInstanceTemplate.deleteInstanceTemplate(PROJECT_ID, INSTANCE_TEMPLATE_NAME);
    assertThat(stdOut.toString())
        .contains("Instance template deletion operation status for " + INSTANCE_TEMPLATE_NAME);
    // Delete the reservation.
    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_NAME);
    assertThat(stdOut.toString())
        .contains("Deleted reservation: " + RESERVATION_NAME);
    stdOut.close();
    System.setOut(out);
  }

  @Test
  public void testCreateReservation()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    CreateReservation.createReservation(
        PROJECT_ID, RESERVATION_NAME, INSTANCE_TEMPLATE_NAME, NUMBER_OF_VMS, ZONE);
    assertThat(stdOut.toString()).contains("Reservation created. Operation Status: DONE");
    // Check if the reservation was created.
    try (ReservationsClient reservationsClient = ReservationsClient.create()) {
      Reservation reservation = reservationsClient.get(PROJECT_ID, ZONE, RESERVATION_NAME);
      assertThat(reservation.getName()).isEqualTo(RESERVATION_NAME);
      assertThat(
          reservation.getSpecificReservation().getCount())
          .isEqualTo(NUMBER_OF_VMS);
    }
  }
}