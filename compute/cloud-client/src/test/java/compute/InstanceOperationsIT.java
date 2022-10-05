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

package compute;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.Instance.Status;
import com.google.cloud.compute.v1.InstancesClient;
import compute.disks.CloneEncryptedDisk;
import compute.disks.DeleteDisk;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 10, unit = TimeUnit.MINUTES)
public class InstanceOperationsIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static String ZONE;
  private static String MACHINE_NAME;
  private static String MACHINE_NAME_ENCRYPTED;
  private static String DISK_NAME;
  private static String RAW_KEY;

  private ByteArrayOutputStream stdOut;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setUp()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    ZONE = "us-central1-a";
    MACHINE_NAME = "my-new-test-instance" + UUID.randomUUID();
    MACHINE_NAME_ENCRYPTED = "encrypted-test-instance" + UUID.randomUUID();
    DISK_NAME = "test-clone-disk-enc-" + UUID.randomUUID();
    RAW_KEY = Util.getBase64EncodedKey();

    // Cleanup existing stale resources.
    Util.cleanUpExistingInstances("my-new-test-instance", PROJECT_ID, ZONE);
    Util.cleanUpExistingInstances("encrypted-test-instance", PROJECT_ID, ZONE);

    compute.CreateInstance.createInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    compute.CreateEncryptedInstance
        .createEncryptedInstance(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED, RAW_KEY);

    TimeUnit.SECONDS.sleep(10);

    stdOut.close();
    System.setOut(out);
  }


  @AfterAll
  public static void cleanup()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    // Delete all instances created for testing.
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED);
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    DeleteDisk.deleteDisk(PROJECT_ID, ZONE, DISK_NAME);

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

  private static Instance getInstance(String machineName) throws IOException {
    try (InstancesClient instancesClient = InstancesClient.create()) {
      return instancesClient.get(PROJECT_ID, ZONE, machineName);
    }
  }

  @Test
  public void testInstanceOperations()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Assert.assertEquals(Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME),
        Status.RUNNING.toString());

    // Stopping the instance.
    StopInstance.stopInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    // Wait for the operation to complete. Setting timeout to 3 mins.
    LocalDateTime endTime = LocalDateTime.now().plusMinutes(3);
    while (!Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME)
        .equalsIgnoreCase(Status.STOPPED.toString())
        && LocalDateTime.now().isBefore(endTime)) {
      TimeUnit.SECONDS.sleep(5);
    }
    Assert.assertEquals(Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME),
        Status.TERMINATED.toString());

    // Starting the instance.
    StartInstance.startInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    // Wait for the operation to complete. Setting timeout to 3 mins.
    endTime = LocalDateTime.now().plusMinutes(3);
    while (!Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME)
        .equalsIgnoreCase(Status.RUNNING.toString())
        && LocalDateTime.now().isBefore(endTime)) {
      TimeUnit.SECONDS.sleep(5);
    }
    Assert.assertEquals(Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME),
        Status.RUNNING.toString());
  }

  @Test
  public void testEncryptedInstanceOperations()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Assert.assertEquals(Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED),
        Status.RUNNING.toString());

    // Stopping the encrypted instance.
    StopInstance.stopInstance(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED);
    // Wait for the operation to complete. Setting timeout to 3 mins.
    LocalDateTime endTime = LocalDateTime.now().plusMinutes(3);
    while (!Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED)
        .equalsIgnoreCase(Status.STOPPED.toString())
        && LocalDateTime.now().isBefore(endTime)) {
      TimeUnit.SECONDS.sleep(5);
    }
    Assert.assertEquals(Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED),
        Status.TERMINATED.toString());

    // Starting the encrypted instance.
    StartEncryptedInstance
        .startEncryptedInstance(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED, RAW_KEY);
    // Wait for the operation to complete. Setting timeout to 3 mins.
    endTime = LocalDateTime.now().plusMinutes(3);
    while (!Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED)
        .equalsIgnoreCase(Status.RUNNING.toString())
        && LocalDateTime.now().isBefore(endTime)) {
      TimeUnit.SECONDS.sleep(5);
    }
    Assert.assertEquals(Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED),
        Status.RUNNING.toString());
  }

  @Test
  public void testCloneEncryptedDisk()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Assert.assertEquals(Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED),
        "RUNNING");
    Instance instance = getInstance(MACHINE_NAME_ENCRYPTED);
    String diskType = String.format("zones/%s/diskTypes/pd-standard", ZONE);
    CloneEncryptedDisk.createDiskFromCustomerEncryptedKey(PROJECT_ID, ZONE, DISK_NAME, diskType, 10,
        instance.getDisks(0).getSource(), RAW_KEY.getBytes(
            StandardCharsets.UTF_8));
    assertThat(stdOut.toString()).contains("Disk cloned with customer encryption key.");
  }

}
