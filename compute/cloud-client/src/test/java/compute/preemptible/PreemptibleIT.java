/*
 * Copyright 2022 Google LLC
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

package compute.preemptible;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.ZoneOperationsClient.ListPagedResponse;
import compute.DeleteInstance;
import compute.Util;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PreemptibleIT {

  @ClassRule
  public Timeout timeout = new Timeout(1, TimeUnit.MINUTES);

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "us-central1-a";
  private static String INSTANCE_NAME;

  private ByteArrayOutputStream stdOut;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    // Cleanup existing test instances.
    Util.cleanUpExistingInstances("preemptible-test-instance", PROJECT_ID, ZONE);

    INSTANCE_NAME = "preemptible-test-instance" + UUID.randomUUID().toString().split("-")[0];

    // Create Instance with Preemptible setting.
    CreatePreemptibleInstance.createPremptibleInstance(PROJECT_ID, ZONE, INSTANCE_NAME);
    assertThat(stdOut.toString()).contains("Instance created : " + INSTANCE_NAME);

    stdOut.close();
    System.setOut(out);
  }

  @AfterAll
  public static void cleanUp()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, INSTANCE_NAME);

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
  public void testIsPreemptible() throws IOException {
    IsPreemptible.isPreemptible(PROJECT_ID, ZONE, INSTANCE_NAME);
    assertThat(stdOut.toString().contains("Preemptible status: true"));
  }

  @Test
  public void testListZoneOperations() throws IOException {
    String filter = String.format(
        "targetLink=\"https://www.googleapis.com/compute/v1/projects/%s/zones/%s/instances/%s\"",
        PROJECT_ID, ZONE, INSTANCE_NAME);
    ListPagedResponse response = ListZoneOperations.listZoneOperations(PROJECT_ID, ZONE, filter);

    boolean elementPresent = false;
    for (Operation operation : response.iterateAll()) {
      elementPresent = true;
      break;
    }

    assertWithMessage(
        "There should be at least one operation for this instance at this point.")
        .that(elementPresent).isTrue();
  }

}
