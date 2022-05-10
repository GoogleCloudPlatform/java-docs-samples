/*
 * Copyright 2021 Google LLC
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

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.Instance.Status;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.UsageExportLocation;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;
import org.junit.Assert;
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
public class SnippetsIT {

  @ClassRule
  public Timeout timeout = new Timeout(1, TimeUnit.SECONDS);

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static String ZONE;
  private static String MACHINE_NAME;
  private static String MACHINE_NAME_DELETE;
  private static String MACHINE_NAME_LIST_INSTANCE;
  private static String MACHINE_NAME_WAIT_FOR_OP;
  private static String MACHINE_NAME_ENCRYPTED;
  private static String BUCKET_NAME;
  private static String IMAGE_PROJECT_NAME;
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
    MACHINE_NAME_DELETE = "my-new-test-instance" + UUID.randomUUID();
    MACHINE_NAME_LIST_INSTANCE = "my-new-test-instance" + UUID.randomUUID();
    MACHINE_NAME_WAIT_FOR_OP = "my-new-test-instance" + UUID.randomUUID();
    MACHINE_NAME_ENCRYPTED = "encrypted-test-instance" + UUID.randomUUID();
    BUCKET_NAME = "my-new-test-bucket" + UUID.randomUUID();
    IMAGE_PROJECT_NAME = "windows-sql-cloud";
    RAW_KEY = getBase64EncodedKey();

    // Cleanup existing stale resources.
    Util.cleanUpExistingInstances("my-new-test-instance", PROJECT_ID, ZONE);
    Util.cleanUpExistingInstances("encrypted-test-instance", PROJECT_ID, ZONE);
    Util.cleanUpExistingInstances("test-instance-", PROJECT_ID, ZONE);

    compute.CreateInstance.createInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    compute.CreateInstance.createInstance(PROJECT_ID, ZONE, MACHINE_NAME_DELETE);
    compute.CreateInstance.createInstance(PROJECT_ID, ZONE, MACHINE_NAME_LIST_INSTANCE);
    compute.CreateInstance.createInstance(PROJECT_ID, ZONE, MACHINE_NAME_WAIT_FOR_OP);
    compute.CreateEncryptedInstance
        .createEncryptedInstance(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED, RAW_KEY);

    TimeUnit.SECONDS.sleep(10);

    // Create a Google Cloud Storage bucket for UsageReports
    Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    storage.create(BucketInfo.of(BUCKET_NAME));

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
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED);
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_LIST_INSTANCE);

    // Delete the Google Cloud Storage bucket created for usage reports.
    Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    Bucket bucket = storage.get(BUCKET_NAME);
    bucket.delete();

    stdOut.close();
    System.setOut(out);
  }


  public static String getBase64EncodedKey() {
    String sampleSpace = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    StringBuilder stringBuilder = new StringBuilder();
    SecureRandom random = new SecureRandom();
    IntStream.range(0, 32)
        .forEach(
            x -> stringBuilder.append(sampleSpace.charAt(random.nextInt(sampleSpace.length()))));

    return Base64.getEncoder()
        .encodeToString(stringBuilder.toString().getBytes(StandardCharsets.US_ASCII));
  }

  public static String getInstanceStatus(String instanceName) throws IOException {
    try (InstancesClient instancesClient = InstancesClient.create()) {
      Instance response = instancesClient.get(PROJECT_ID, ZONE, instanceName);
      return response.getStatus();
    }
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
  public void testCreateInstance() throws IOException {
    // Check if the instance was successfully created during the setup.
    String response = getInstanceStatus(MACHINE_NAME);
    Assert.assertEquals(response, Status.RUNNING.toString());
  }

  @Test
  public void testCreateEncryptedInstance() throws IOException {
    // Check if the instance was successfully created during the setup.
    String response = getInstanceStatus(MACHINE_NAME_ENCRYPTED);
    Assert.assertEquals(response, Status.RUNNING.toString());
  }


  @Test
  public void testListInstance() throws IOException {
    compute.ListInstance.listInstances(PROJECT_ID, ZONE);
    assertThat(stdOut.toString()).contains(MACHINE_NAME_LIST_INSTANCE);
  }

  @Test
  public void testListAllInstances() throws IOException {
    compute.ListAllInstances.listAllInstances(PROJECT_ID);
    assertThat(stdOut.toString()).contains(MACHINE_NAME_LIST_INSTANCE);
  }

  @Test
  public void testDeleteInstance()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_DELETE);
    assertThat(stdOut.toString()).contains("Operation Status: DONE");
  }

  @Test
  public void testWaitForOperation()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // Construct a delete request and get the operation instance.
    InstancesClient instancesClient = InstancesClient.create();
    OperationFuture<Operation, Operation> operation = instancesClient.deleteAsync(PROJECT_ID, ZONE,
        MACHINE_NAME_WAIT_FOR_OP);
    // Wait for the operation to complete.
    operation.get(3, TimeUnit.MINUTES);
    assertThat(stdOut.toString().contains("Operation Status: DONE"));
  }

  @Test
  public void testSetUsageBucketExportCustomPrefix()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // Set custom Report Name Prefix.
    String customPrefix = "my-custom-prefix";
    compute.SetUsageExportBucket.setUsageExportBucket(PROJECT_ID, BUCKET_NAME, customPrefix);
    assertThat(stdOut.toString()).doesNotContain("default value of `usage_gce`");
    assertThat(stdOut.toString().contains("Operation Status: DONE"));

    // Wait for the settings to take place.
    TimeUnit.SECONDS.sleep(10);

    UsageExportLocation usageExportLocation = compute.SetUsageExportBucket
        .getUsageExportBucket(PROJECT_ID);
    assertThat(stdOut.toString()).doesNotContain("default value of `usage_gce`");
    Assert.assertEquals(usageExportLocation.getBucketName(), BUCKET_NAME);
    Assert.assertEquals(usageExportLocation.getReportNamePrefix(), customPrefix);

    // Disable usage exports.
    boolean isDisabled = compute.SetUsageExportBucket.disableUsageExportBucket(PROJECT_ID);
    Assert.assertFalse(isDisabled);
  }

  @Test
  public void testListImages() throws IOException {
    // =================== Flat list of images ===================
    ListImages.listImages(IMAGE_PROJECT_NAME);
    int imageCount = Integer.parseInt(stdOut.toString().split(":")[1].trim());
    Assert.assertTrue(imageCount > 2);
  }

  @Test
  public void testListImagesByPage() throws IOException {
    // ================= Paginated list of images ================
    ListImages.listImagesByPage(IMAGE_PROJECT_NAME, 2);
    Assert.assertTrue(stdOut.toString().contains("Page Number: 1"));
  }

  @Test
  public void testInstanceOperations()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Assert.assertEquals(getInstanceStatus(MACHINE_NAME), Status.RUNNING.toString());

    // Stopping the instance.
    StopInstance.stopInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    // Wait for the operation to complete. Setting timeout to 3 mins.
    LocalDateTime endTime = LocalDateTime.now().plusMinutes(3);
    while (getInstanceStatus(MACHINE_NAME).equalsIgnoreCase(Status.STOPPING.toString())
        && LocalDateTime.now().isBefore(endTime)) {
      TimeUnit.SECONDS.sleep(5);
    }
    Assert.assertEquals(getInstanceStatus(MACHINE_NAME), Status.TERMINATED.toString());

    // Starting the instance.
    StartInstance.startInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    // Wait for the operation to complete. Setting timeout to 3 mins.
    endTime = LocalDateTime.now().plusMinutes(3);
    while (getInstanceStatus(MACHINE_NAME).equalsIgnoreCase(Status.RUNNING.toString())
        && LocalDateTime.now().isBefore(endTime)) {
      TimeUnit.SECONDS.sleep(5);
    }
    Assert.assertEquals(getInstanceStatus(MACHINE_NAME), Status.RUNNING.toString());
  }

  @Test
  public void testEncryptedInstanceOperations()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Assert.assertEquals(getInstanceStatus(MACHINE_NAME_ENCRYPTED), Status.RUNNING.toString());

    // Stopping the encrypted instance.
    StopInstance.stopInstance(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED);
    // Wait for the operation to complete. Setting timeout to 3 mins.
    LocalDateTime endTime = LocalDateTime.now().plusMinutes(3);
    while (getInstanceStatus(MACHINE_NAME_ENCRYPTED).equalsIgnoreCase(Status.STOPPING.toString())
        && LocalDateTime.now().isBefore(endTime)) {
      TimeUnit.SECONDS.sleep(5);
    }
    Assert.assertEquals(getInstanceStatus(MACHINE_NAME_ENCRYPTED), Status.TERMINATED.toString());

    // Starting the encrypted instance.
    StartEncryptedInstance
        .startEncryptedInstance(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED, RAW_KEY);
    // Wait for the operation to complete. Setting timeout to 3 mins.
    endTime = LocalDateTime.now().plusMinutes(3);
    while (getInstanceStatus(MACHINE_NAME_ENCRYPTED).equalsIgnoreCase(Status.RUNNING.toString())
        && LocalDateTime.now().isBefore(endTime)) {
      TimeUnit.SECONDS.sleep(5);
    }
    Assert.assertEquals(getInstanceStatus(MACHINE_NAME_ENCRYPTED), Status.RUNNING.toString());
  }

}
