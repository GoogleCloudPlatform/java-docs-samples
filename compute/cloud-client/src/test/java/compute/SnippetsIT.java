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
import static compute.Util.getEnvVar;
import static compute.Util.getZone;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.Instance.Status;
import com.google.cloud.compute.v1.InstanceTemplate;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.UsageExportLocation;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.Rule;
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
public class SnippetsIT {
  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(3);

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String TEST_IMAGE_PROJECT_NAME = "JAVA_DOCS_COMPUTE_TEST_IMAGE_PROJECT";
  private static final String ZONE = getZone();
  private static final String REGION = ZONE.substring(0, ZONE.lastIndexOf('-'));
  private static String MACHINE_NAME;
  private static String MACHINE_NAME_LIST_INSTANCE;
  private static String MACHINE_NAME_WAIT_FOR_OP;
  private static String MACHINE_NAME_ENCRYPTED;
  private static String MACHINE_NAME_WITH_SSD;
  private static String BUCKET_NAME;
  private static String IMAGE_PROJECT_NAME;
  private static String RAW_KEY;
  private static String REGIONAL_LOCATION_NAME;

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

    MACHINE_NAME = "my-new-test-instance-" + UUID.randomUUID();
    MACHINE_NAME_LIST_INSTANCE = "my-new-test-instance-" + UUID.randomUUID();
    MACHINE_NAME_WAIT_FOR_OP = "my-new-test-instance-" + UUID.randomUUID();
    MACHINE_NAME_ENCRYPTED = "encrypted-test-instance-" + UUID.randomUUID();
    MACHINE_NAME_WITH_SSD = "test-instance-with-ssd-" + UUID.randomUUID();
    REGIONAL_LOCATION_NAME = "test-inst-temp-regional-" + UUID.randomUUID();
    BUCKET_NAME = "my-new-test-bucket" + UUID.randomUUID();
    IMAGE_PROJECT_NAME = getEnvVar(TEST_IMAGE_PROJECT_NAME, "windows-sql-cloud");
    RAW_KEY = Util.getBase64EncodedKey();

    // Cleanup existing stale resources.
    Util.cleanUpExistingInstances("my-new-test-instance-", PROJECT_ID, ZONE);
    Util.cleanUpExistingInstances("encrypted-test-instance-", PROJECT_ID, ZONE);
    Util.cleanUpExistingInstances("test-instance-with-ssd-", PROJECT_ID, ZONE);
    Util.cleanUpExistingRegionalInstanceTemplates("test-inst-temp-regional", PROJECT_ID, ZONE);

    compute.CreateInstance.createInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    compute.CreateInstance.createInstance(PROJECT_ID, ZONE, MACHINE_NAME_LIST_INSTANCE);
    compute.CreateInstance.createInstance(PROJECT_ID, ZONE, MACHINE_NAME_WAIT_FOR_OP);
    compute.CreateEncryptedInstance
        .createEncryptedInstance(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED, RAW_KEY);
    CreateRegionalInstanceTemplate
        .createRegionalInstanceTemplate(PROJECT_ID, REGION, REGIONAL_LOCATION_NAME);

    TimeUnit.SECONDS.sleep(30);

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
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_WITH_SSD);
    DeleteRegionalInstanceTemplate
        .deleteRegionalInstanceTemplate(PROJECT_ID, REGION, REGIONAL_LOCATION_NAME);

    // Delete the Google Cloud Storage bucket created for usage reports.
    Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    Bucket bucket = storage.get(BUCKET_NAME);
    bucket.delete();

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
  public void testCreateInstance() throws IOException {
    // Check if the instance was successfully created during the setup.
    String response = Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME);
    Assert.assertEquals(response, Status.RUNNING.toString());
  }

  @Test
  public void testGetInstance() throws IOException {
    GetInstance.getInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    assertThat(stdOut.toString()).contains("Retrieved the instance");
  }

  @Test
  public void testCreateEncryptedInstance() throws IOException {
    // Check if the instance was successfully created during the setup.
    String response = Util.getInstanceStatus(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED);
    Assert.assertEquals(response, Status.RUNNING.toString());
  }

  @Test
  public void testCreateWithLocalSSD()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Assert that the instance is created.
    CreateWithLocalSsd.createWithLocalSsd(PROJECT_ID, ZONE, MACHINE_NAME_WITH_SSD);
    assertThat(stdOut.toString()).contains("Instance created with local SSD:");

    try (InstancesClient instancesClient = InstancesClient.create()) {
      Instance instance = instancesClient.get(PROJECT_ID, ZONE, MACHINE_NAME_WITH_SSD);
      // Assert that atleast one of the disks has the type "SCRATCH".
      Assert.assertTrue(instance.getDisksList().stream()
          .anyMatch(disk -> disk.getType().equalsIgnoreCase(AttachedDisk.Type.SCRATCH.name())));
      // Assert that there are only 2 disks present.
      Assert.assertEquals(instance.getDisksList().size(), 2);
    }
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
  public void testWaitForOperation()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // Construct a delete request and get the operation instance.
    InstancesClient instancesClient = InstancesClient.create();
    OperationFuture<Operation, Operation> operation = instancesClient.deleteAsync(PROJECT_ID, ZONE,
        MACHINE_NAME_WAIT_FOR_OP);
    // Wait for the operation to complete.
    operation.get(5, TimeUnit.MINUTES);
    assertThat(stdOut.toString().contains("Operation Status: DONE"));
  }

  @Test
  public void testSetUsageBucketExportCustomPrefix()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // Set custom Report Name Prefix.
    String customPrefix = "my-custom-prefix";
    compute.SetUsageExportBucket.setUsageExportBucket(PROJECT_ID, BUCKET_NAME, customPrefix);
    Assert.assertFalse(stdOut.toString().contains("default value of `usage_gce`"));
    Assert.assertTrue(stdOut.toString().contains("Operation Status: DONE"));

    UsageExportLocation usageExportLocation = compute.SetUsageExportBucket
        .getUsageExportBucket(PROJECT_ID);

    // Wait for the settings to take place.
    TimeUnit.MINUTES.sleep(3);
    assertThat(stdOut.toString()).doesNotContain("default value of `usage_gce`");
    Assert.assertNotNull(usageExportLocation.getBucketName());
    Assert.assertEquals(usageExportLocation.getBucketName(), BUCKET_NAME);
    Assert.assertEquals(usageExportLocation.getReportNamePrefix(), customPrefix);

    // Disable usage exports.
    boolean isDisabled = compute.SetUsageExportBucket.disableUsageExportBucket(PROJECT_ID);
    // Wait for the settings to take place.
    TimeUnit.MINUTES.sleep(2);

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
  public void testGetRegionalInstanceTemplate() throws IOException {
    // Check if the instance was successfully created during the setup.
    InstanceTemplate instanceTemplate = GetRegionalInstanceTemplate
        .getRegionalInstanceTemplate(PROJECT_ID, REGION,
        REGIONAL_LOCATION_NAME);
    Assert.assertEquals(REGIONAL_LOCATION_NAME, instanceTemplate.getName());
  }
}
