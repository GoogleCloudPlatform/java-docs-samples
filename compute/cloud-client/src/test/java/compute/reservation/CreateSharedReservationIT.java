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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(value = 300, unit = TimeUnit.SECONDS)
public class CreateSharedReservationIT {

  private static String PROJECT_ID;
  private static String ZONE;
  private static String RESERVATION_NAME;
  private static String[] CONSUMER_PROJECT_IDS;
  private ByteArrayOutputStream stdOut;

  @BeforeAll
  public static void setUp()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {

    PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
    ZONE = "us-central1-a";
    RESERVATION_NAME = "test-reservation-" + UUID.randomUUID();
    CONSUMER_PROJECT_IDS = new String[]{"CONSUMER_PROJECT_ID_1", "CONSUMER_PROJECT_ID_2"};

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
  public static void tearDown()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    //    DeleteReservation.deleteReservation(PROJECT_ID, ZONE, RESERVATION_NAME);
    //    assertThat(stdOut.toString()).contains("Deleted reservation: " + RESERVATION_NAME);

    stdOut.close();
    System.setOut(out);
  }

  @Test
  public void testCreateSharedReservation()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    CreateSharedReservation.createSharedReservation(
        PROJECT_ID,
        ZONE,
        RESERVATION_NAME,
        "global",
        "YOUR_INSTANCE_TEMPLATE_NAME",
        1,
        CONSUMER_PROJECT_IDS);

    assertThat(stdOut.toString()).contains("Reservation created. Operation Status: DONE");
  }
}