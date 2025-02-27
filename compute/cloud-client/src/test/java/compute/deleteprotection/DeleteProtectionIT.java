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

package compute.deleteprotection;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import compute.DeleteInstance;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 10, unit = TimeUnit.MINUTES)
public class DeleteProtectionIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "asia-south1-a";
  private static String INSTANCE_NAME;

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
    INSTANCE_NAME = "delete-protect-test-instance" + UUID.randomUUID().toString().split("-")[0];

    // Cleanup existing test instances.
    Util.cleanUpExistingInstances("delete-protect-test-instance", PROJECT_ID, ZONE);

    // Create Instance with Delete Protection.
    CreateInstanceDeleteProtection.createInstanceDeleteProtection(PROJECT_ID, ZONE, INSTANCE_NAME,
        true);
    assertThat(stdOut.toString()).contains("Instance created : " + INSTANCE_NAME);

    stdOut.close();
    System.setOut(out);
  }

  @AfterAll
  public static void cleanUp()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {

    // If cleanup is pre-maturely executed, then manually unset Delete Protection bit.
    if (GetDeleteProtection.getDeleteProtection(PROJECT_ID, ZONE, INSTANCE_NAME)) {
      SetDeleteProtection.setDeleteProtection(PROJECT_ID, ZONE, INSTANCE_NAME, false);
    }
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, INSTANCE_NAME);
  }

  @Test
  public void testDeleteProtection()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Assert.assertTrue(GetDeleteProtection.getDeleteProtection(PROJECT_ID, ZONE, INSTANCE_NAME));
    SetDeleteProtection.setDeleteProtection(PROJECT_ID, ZONE, INSTANCE_NAME, false);
    Assert.assertFalse(GetDeleteProtection.getDeleteProtection(PROJECT_ID, ZONE, INSTANCE_NAME));

  }
}