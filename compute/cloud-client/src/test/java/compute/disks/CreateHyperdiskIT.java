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

package compute.disks;

import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.compute.v1.Disk;
import java.io.IOException;
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
@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class CreateHyperdiskIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "us-west1-a";
  private static final String  HYPERDISK_NAME = "test-hyperdisk-enc-" + UUID.randomUUID();

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
  }

  @AfterAll
  public static void cleanup()
       throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // Delete disk created for testing.
    DeleteDisk.deleteDisk(PROJECT_ID, ZONE, HYPERDISK_NAME);
  }

  @Test
  public void testCreateHyperdisk()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String diskType = String.format("zones/%s/diskTypes/hyperdisk-balanced", ZONE);

    Disk hyperdisk = CreateHyperdisk
        .createHyperdisk(PROJECT_ID, ZONE, HYPERDISK_NAME, diskType,
            10, 3000, 140);

    Assert.assertNotNull(hyperdisk);
    Assert.assertEquals(HYPERDISK_NAME, hyperdisk.getName());
    Assert.assertEquals(3000, hyperdisk.getProvisionedIops());
    Assert.assertEquals(140, hyperdisk.getProvisionedThroughput());
    Assert.assertEquals(10, hyperdisk.getSizeGb());
    Assert.assertTrue(hyperdisk.getType().contains("hyperdisk-balanced"));
    Assert.assertTrue(hyperdisk.getZone().contains(ZONE));
  }
}