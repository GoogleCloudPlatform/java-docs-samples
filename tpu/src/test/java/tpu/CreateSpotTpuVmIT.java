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

package tpu;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.assertTrue;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.tpu.v2.Node;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 6, unit = TimeUnit.MINUTES)
public class CreateSpotTpuVmIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "us-east5-a";
  static String javaVersion = System.getProperty("java.version").substring(0, 2);
  private static final String NODE_NAME = "test-spot-tpu-" + javaVersion + "-"
      + UUID.randomUUID().toString().substring(0, 8);
  private static final String TPU_TYPE = "v5p-8";
  private static final String TPU_SOFTWARE_VERSION = "tpu-vm-tf-2.14.1";
  private static final String NODE_PATH_NAME =
      String.format("projects/%s/locations/%s/nodes/%s", PROJECT_ID, ZONE, NODE_NAME);

  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setUp()
      throws IOException, ExecutionException, InterruptedException {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    // Cleanup existing stale resources.
    Util.cleanUpExistingTpu("test-spot-tpu-" + javaVersion, PROJECT_ID, ZONE);
  }

  @AfterAll
  public static void cleanup() throws Exception {
    DeleteTpuVm.deleteTpuVm(PROJECT_ID, ZONE, NODE_NAME);

    // Test that TPUs is deleted
    Assertions.assertThrows(
        NotFoundException.class,
        () -> GetTpuVm.getTpuVm(PROJECT_ID, ZONE, NODE_NAME));
  }

  @Test
  public void testCreateSpotTpuVm() throws IOException, ExecutionException, InterruptedException {
    Node node = CreateSpotTpuVm.createSpotTpuVm(
        PROJECT_ID, ZONE, NODE_NAME, TPU_TYPE, TPU_SOFTWARE_VERSION);

    assertThat(node.getName()).isEqualTo(NODE_PATH_NAME);
    assertTrue(node.getSchedulingConfig().getPreemptible());

  }
}