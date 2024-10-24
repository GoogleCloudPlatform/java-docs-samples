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

import static com.google.cloud.tpu.v2.Node.State.READY;
import static com.google.cloud.tpu.v2.Node.State.STOPPED;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.assertNotNull;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.tpu.v2.Node;
import com.google.cloud.tpu.v2.TpuClient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 25, unit = TimeUnit.MINUTES)
@TestMethodOrder(MethodOrderer. OrderAnnotation. class)
public class TpuVmIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "us-central1-c";
  static String javaVersion = System.getProperty("java.version").substring(0, 2);
  private static final String TPU_VM_NAME = "test-tpu-" + javaVersion + "-"
      + UUID.randomUUID().toString().substring(0, 8);
  private static final String ACCELERATOR_TYPE = "v2-8";
  private static final String VERSION = "tpu-vm-tf-2.14.1";
  private static final String TPU_VM_PATH_NAME =
      String.format("projects/%s/locations/%s/nodes/%s", PROJECT_ID, ZONE, TPU_VM_NAME);

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
    Util.cleanUpExistingTpu("test-tpu-" + javaVersion, PROJECT_ID, ZONE);
  }

  @AfterAll
  public static void cleanup() throws Exception {
    DeleteTpuVm.deleteTpuVm(PROJECT_ID, ZONE, TPU_VM_NAME);

    // Test that TPUs is deleted
    Assertions.assertThrows(
        NotFoundException.class,
        () -> GetTpuVm.getTpuVm(PROJECT_ID, ZONE, TPU_VM_NAME));
  }

  @Test
  @Order(1)
  public void testCreateTpuVm() throws IOException, ExecutionException, InterruptedException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    CreateTpuVm.createTpuVm(PROJECT_ID, ZONE, TPU_VM_NAME, ACCELERATOR_TYPE, VERSION);

    assertThat(stdOut.toString()).contains("TPU VM created: " + TPU_VM_PATH_NAME);
    stdOut.close();
    System.setOut(out);
  }

  @Test
  @Order(2)
  public void testGetTpuVm() throws IOException {
    Node node = GetTpuVm.getTpuVm(PROJECT_ID, ZONE, TPU_VM_NAME);

    assertNotNull(node);
    assertThat(node.getName()).isEqualTo(TPU_VM_PATH_NAME);
  }

  @Test
  @Order(2)
  public void testListTpuVm() throws IOException {
    TpuClient.ListNodesPagedResponse nodesList = ListTpuVms.listTpuVms(PROJECT_ID, ZONE);

    assertNotNull(nodesList);
    for (Node node : nodesList.iterateAll()) {
      Assert.assertTrue(node.getName().contains("test-tpu"));
    }
  }

  @Test
  @Order(2)
  public void testStopTpuVm() throws IOException, ExecutionException, InterruptedException {
    StopTpuVm.stopTpuVm(PROJECT_ID, ZONE, TPU_VM_NAME);
    Node node = GetTpuVm.getTpuVm(PROJECT_ID, ZONE, TPU_VM_NAME);

    assertThat(node.getState()).isEqualTo(STOPPED);
  }

  @Test
  @Order(3)
  public void testStartTpuVm() throws IOException, ExecutionException, InterruptedException {
    StartTpuVm.startTpuVm(PROJECT_ID, ZONE, TPU_VM_NAME);
    Node node = GetTpuVm.getTpuVm(PROJECT_ID, ZONE, TPU_VM_NAME);

    assertThat(node.getState()).isEqualTo(READY);
  }
}