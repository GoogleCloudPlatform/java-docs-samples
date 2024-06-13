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

package compute.spots;

import static com.google.common.truth.Truth.assertWithMessage;
import static compute.Util.getZone;

import com.google.cloud.compute.v1.Instance;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import compute.DeleteInstance;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

@RunWith(JUnit4.class)
@Timeout(value = 10, unit = TimeUnit.MINUTES)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SpotVmIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = getZone();
  private static String INSTANCE_NAME;
  private static final int MAX_ATTEMPT_COUNT = 3;
  private static final int INITIAL_BACKOFF_MILLIS = 180000; // 3 minutes

  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(
      MAX_ATTEMPT_COUNT,
      INITIAL_BACKOFF_MILLIS);

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
            .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeClass
  public static void setUp() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    INSTANCE_NAME = "my-new-spot-instance-" + UUID.randomUUID();
  }

  @AfterClass
  public static void cleanup()
          throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // Delete all instances created for testing.
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, INSTANCE_NAME);
  }

  @Test
  public void stage1_CreateSpot()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Instance spotInstance = CreateSpotVm.createSpotInstance(PROJECT_ID, INSTANCE_NAME, ZONE);
    Assert.assertNotNull(spotInstance);
    Assert.assertTrue(spotInstance.getZone().contains(ZONE));
    Assert.assertEquals(INSTANCE_NAME, spotInstance.getName());
    Assert.assertFalse(spotInstance.getDisksList().isEmpty());
  }

  @Test
  public void stage2_GetSpot() throws IOException {
    Assert.assertTrue(CheckIsSpotVm.isSpotVm(PROJECT_ID, INSTANCE_NAME, ZONE));
  }
}
