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
import static org.junit.Assert.assertEquals;

import com.google.cloud.compute.v1.Operation;
import compute.Util;
import compute.disks.consistencygroup.CreateDiskConsistencyGroup;
import compute.disks.consistencygroup.DeleteDiskConsistencyGroup;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class ConsistencyGroupIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION = "us-central1";
  private static final String CONSISTENCY_GROUP_NAME =
      "test-consistency-group-" + UUID.randomUUID();

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setUp() throws Exception {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
    Util.cleanUpExistingConsistencyGroup("test-consistency-group-", PROJECT_ID, REGION);
  }

  @AfterAll
  public static void cleanUp()
      throws IOException, ExecutionException, InterruptedException {
    // Delete created consistency group
    Operation.Status status = DeleteDiskConsistencyGroup.deleteDiskConsistencyGroup(
        PROJECT_ID, REGION, CONSISTENCY_GROUP_NAME);

    assertEquals(Operation.Status.DONE, status);
  }

  @Test
  public void testCreateDiskConsistencyGroupResourcePolicy()
      throws IOException, ExecutionException, InterruptedException {
    Operation.Status status = CreateDiskConsistencyGroup.createDiskConsistencyGroup(
            PROJECT_ID, REGION, CONSISTENCY_GROUP_NAME);

    assertEquals(Operation.Status.DONE, status);
  }
}
