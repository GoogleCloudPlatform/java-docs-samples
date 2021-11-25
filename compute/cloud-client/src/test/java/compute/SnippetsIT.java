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

import com.google.api.gax.rpc.InvalidArgumentException;
import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.FirewallsClient;
import com.google.cloud.compute.v1.GlobalOperationsClient;
import com.google.cloud.compute.v1.Image;
import com.google.cloud.compute.v1.ImagesClient;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.Instance.Status;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Snapshot;
import com.google.cloud.compute.v1.SnapshotsClient;
import com.google.cloud.compute.v1.UsageExportLocation;
import com.google.cloud.compute.v1.ZoneOperationsClient;
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
import java.util.stream.IntStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SnippetsIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static String ZONE;
  private static String MACHINE_NAME;
  private static String MACHINE_NAME_DELETE;
  private static String MACHINE_NAME_LIST_INSTANCE;
  private static String MACHINE_NAME_WAIT_FOR_OP;
  private static String MACHINE_NAME_ENCRYPTED;
  private static String MACHINE_NAME_PUBLIC_IMAGE;
  private static String MACHINE_NAME_CUSTOM_IMAGE;
  private static String MACHINE_NAME_ADDITIONAL_DISK;
  private static String MACHINE_NAME_SNAPSHOT;
  private static String MACHINE_NAME_SNAPSHOT_ADDITIONAL;
  private static String MACHINE_NAME_SUBNETWORK;
  private static String BUCKET_NAME;
  private static String IMAGE_PROJECT_NAME;
  private static String FIREWALL_RULE_CREATE;
  private static String NETWORK_NAME;
  private static String SUBNETWORK_NAME;
  private static String RAW_KEY;
  private static Disk TEST_DISK;
  private static Image TEST_IMAGE;
  private static Snapshot TEST_SNAPSHOT;

  private ByteArrayOutputStream stdOut;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  private static Image getActiveDebian()
      throws IOException, InterruptedException, ExecutionException {
    try (ImagesClient imagesClient = ImagesClient.create()) {
      Image image = imagesClient.getFromFamily("debian-cloud", "debian-10");
      return image;
    }
  }

  private static Disk createSourceDisk()
      throws IOException, InterruptedException, ExecutionException {
    try (DisksClient disksClient = DisksClient.create();
        ZoneOperationsClient zoneOperationsClient = ZoneOperationsClient.create()) {

      Disk disk = Disk.newBuilder()
          .setSourceImage(getActiveDebian().getSelfLink())
          .setName("test-disk-" + UUID.randomUUID())
          .build();

      Operation operation = disksClient.insert(PROJECT_ID, ZONE, disk);
      // Wait for the operation to complete.
      Operation response = zoneOperationsClient.wait(PROJECT_ID, ZONE, operation.getName());
      return disksClient.get(PROJECT_ID, ZONE, disk.getName());
    }
  }

  private static void deleteDisk(Disk disk)
      throws IOException, InterruptedException, ExecutionException {
    try (DisksClient disksClient = DisksClient.create();
        ZoneOperationsClient zoneOperationsClient = ZoneOperationsClient.create()) {
      Operation operation = disksClient.delete(PROJECT_ID, ZONE, disk.getName());
      Operation response = zoneOperationsClient.wait(PROJECT_ID, ZONE, operation.getName());
      return;
    }
  }

  private static Snapshot createSnapshot(Disk srcDisk)
      throws IOException, InterruptedException, ExecutionException {
    try (SnapshotsClient snapshotsClient = SnapshotsClient.create();
        DisksClient disksClient = DisksClient.create();
        ZoneOperationsClient zoneOperationsClient = ZoneOperationsClient.create()) {

      Snapshot snapshot = Snapshot.newBuilder()
          .setName("test-snap-" + UUID.randomUUID())
          .build();

      Operation operation = disksClient.createSnapshot(PROJECT_ID, ZONE, srcDisk.getName(),
          snapshot);
      Operation response = zoneOperationsClient.wait(PROJECT_ID, ZONE, operation.getName());
      return snapshotsClient.get(PROJECT_ID, snapshot.getName());
    }
  }

  private static void deleteSnapshot(Snapshot snapshot)
      throws IOException, InterruptedException, ExecutionException {
    try (SnapshotsClient snapshotsClient = SnapshotsClient.create();
        GlobalOperationsClient globalOperationsClient = GlobalOperationsClient.create()) {
      Operation operation = snapshotsClient.delete(PROJECT_ID, snapshot.getName());
      Operation response = globalOperationsClient.wait(PROJECT_ID, operation.getName());
      return;
    }
  }

  private static Image createImage(Disk srcDisk)
      throws IOException, InterruptedException, ExecutionException {
    try (ImagesClient imagesClient = ImagesClient.create();
        DisksClient disksClient = DisksClient.create();
        GlobalOperationsClient globalOperationsClient = GlobalOperationsClient.create()) {

      Image image = Image.newBuilder()
          .setName("test-img-" + UUID.randomUUID())
          .setSourceDisk(srcDisk.getSelfLink())
          .build();

      Operation operation = imagesClient.insert(PROJECT_ID, image);
      Operation response = globalOperationsClient.wait(PROJECT_ID, operation.getName());
      return imagesClient.get(PROJECT_ID, image.getName());
    }
  }

  private static void deleteImage(Image image)
      throws IOException, InterruptedException, ExecutionException {
    try (ImagesClient imagesClient = ImagesClient.create();
        GlobalOperationsClient globalOperationsClient = GlobalOperationsClient.create()) {
      Operation operation = imagesClient.delete(PROJECT_ID, image.getName());
      Operation response = globalOperationsClient.wait(PROJECT_ID, operation.getName());
      return;
    }
  }

  @BeforeClass
  public static void setUp() throws IOException, InterruptedException, ExecutionException {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    ZONE = "us-central1-a";
    MACHINE_NAME = "my-new-test-instance" + UUID.randomUUID();
    MACHINE_NAME_DELETE = "my-new-test-instance" + UUID.randomUUID();
    MACHINE_NAME_LIST_INSTANCE = "my-new-test-instance" + UUID.randomUUID();
    MACHINE_NAME_WAIT_FOR_OP = "my-new-test-instance" + UUID.randomUUID();
    MACHINE_NAME_ENCRYPTED = "encrypted-test-instance" + UUID.randomUUID();
    MACHINE_NAME_PUBLIC_IMAGE = "test-instance-pub-" + UUID.randomUUID();
    MACHINE_NAME_CUSTOM_IMAGE = "test-instance-cust-" + UUID.randomUUID();
    MACHINE_NAME_ADDITIONAL_DISK = "test-instance-add-" + UUID.randomUUID();
    MACHINE_NAME_SNAPSHOT = "test-instance-snap-" + UUID.randomUUID();
    MACHINE_NAME_SNAPSHOT_ADDITIONAL = "test-instance-snapa-" + UUID.randomUUID();
    MACHINE_NAME_SUBNETWORK = "test-instance-subnet-" + UUID.randomUUID();
    BUCKET_NAME = "my-new-test-bucket" + UUID.randomUUID();
    IMAGE_PROJECT_NAME = "windows-sql-cloud";
    FIREWALL_RULE_CREATE = "firewall-rule-" + UUID.randomUUID();
    NETWORK_NAME = "global/networks/default";
    SUBNETWORK_NAME = "regions/us-central1/subnetworks/default";
    RAW_KEY = getBase64EncodedKey();

    compute.CreateInstance.createInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    compute.CreateInstance.createInstance(PROJECT_ID, ZONE, MACHINE_NAME_DELETE);
    compute.CreateInstance.createInstance(PROJECT_ID, ZONE, MACHINE_NAME_LIST_INSTANCE);
    compute.CreateInstance.createInstance(PROJECT_ID, ZONE, MACHINE_NAME_WAIT_FOR_OP);
    compute.CreateBulkInstances.createBulkInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    compute.CreateEncryptedInstance
        .createEncryptedInstance(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED, RAW_KEY);

    TEST_DISK = createSourceDisk();
    TEST_SNAPSHOT = createSnapshot(TEST_DISK);
    TEST_IMAGE = createImage(TEST_DISK);

    compute.CreateInstancesAdvanced.createFromPublicImage(PROJECT_ID, ZONE,
        MACHINE_NAME_PUBLIC_IMAGE);
    compute.CreateInstancesAdvanced.createFromCustomImage(PROJECT_ID, ZONE,
        MACHINE_NAME_CUSTOM_IMAGE, TEST_IMAGE.getSelfLink());
    compute.CreateInstancesAdvanced.createWithAdditionalDisk(PROJECT_ID, ZONE,
        MACHINE_NAME_ADDITIONAL_DISK);
    compute.CreateInstancesAdvanced.createFromSnapshot(PROJECT_ID, ZONE, MACHINE_NAME_SNAPSHOT,
        TEST_SNAPSHOT.getSelfLink());
    compute.CreateInstancesAdvanced.createWithSnapshottedDataDisk(PROJECT_ID, ZONE,
        MACHINE_NAME_SNAPSHOT_ADDITIONAL, TEST_SNAPSHOT.getSelfLink());
    compute.CreateInstancesAdvanced.createWithSubnetwork(PROJECT_ID, ZONE, MACHINE_NAME_SUBNETWORK,
        NETWORK_NAME, SUBNETWORK_NAME);

    TimeUnit.SECONDS.sleep(10);
    compute.CreateFirewallRule.createFirewall(PROJECT_ID, FIREWALL_RULE_CREATE, NETWORK_NAME);
    TimeUnit.SECONDS.sleep(10);
    // Moving the following tests to setup section as the created firewall rule is auto-deleted
    // by GCE Enforcer within a few minutes.
    testListFirewallRules();
    testPatchFirewallRule();

    // Create a Google Cloud Storage bucket for UsageReports
    Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    storage.create(BucketInfo.of(BUCKET_NAME));
  }


  @AfterClass
  public static void cleanup() throws IOException, InterruptedException, ExecutionException {
    // Delete all instances created for testing.
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    deleteFirewallRuleIfNotDeletedByGceEnforcer(PROJECT_ID, FIREWALL_RULE_CREATE);
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED);
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_LIST_INSTANCE);

    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_PUBLIC_IMAGE);
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_CUSTOM_IMAGE);
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_ADDITIONAL_DISK);
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_SNAPSHOT);
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_SNAPSHOT_ADDITIONAL);
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_SUBNETWORK);

    deleteImage(TEST_IMAGE);
    deleteSnapshot(TEST_SNAPSHOT);
    deleteDisk(TEST_DISK);

    // Delete the Google Cloud Storage bucket created for usage reports.
    Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    Bucket bucket = storage.get(BUCKET_NAME);
    bucket.delete();

    stdOut.close();
    System.setOut(null);
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

  public static void testListFirewallRules() throws IOException {
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    compute.ListFirewallRules.listFirewallRules(PROJECT_ID);
    assertThat(stdOut.toString()).contains(FIREWALL_RULE_CREATE);
    stdOut.close();
    System.setOut(null);
  }

  public static void testPatchFirewallRule() throws IOException, InterruptedException {
    try (FirewallsClient client = FirewallsClient.create()) {
      ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
      System.setOut(new PrintStream(stdOut));
      Assert.assertEquals(1000, client.get(PROJECT_ID, FIREWALL_RULE_CREATE).getPriority());
      compute.PatchFirewallRule.patchFirewallPriority(PROJECT_ID, FIREWALL_RULE_CREATE, 500);
      TimeUnit.SECONDS.sleep(5);
      Assert.assertEquals(500, client.get(PROJECT_ID, FIREWALL_RULE_CREATE).getPriority());
      stdOut.close();
      System.setOut(null);
    }
  }

  public static void deleteFirewallRuleIfNotDeletedByGceEnforcer(String projectId,
      String firewallRule) throws IOException {
    /* (**INTERNAL method**)
      This method will prevent test failure if the firewall rule was auto-deleted by GCE Enforcer.
      (Feel free to remove this method if not running on a Google-owned project.)
     */
    try {
      GetFirewallRule.getFirewallRule(projectId, firewallRule);
      DeleteFirewallRule.deleteFirewallRule(projectId, firewallRule);
    } catch (NotFoundException e) {
      System.out.println("Rule already deleted ! ");
      return;
    } catch (InvalidArgumentException e) {
      System.out.println("Rule is not ready (probably being deleted).");
      return;
    }
  }

  public static Status getInstanceStatus(String instanceName) throws IOException {
    try (InstancesClient instancesClient = InstancesClient.create()) {
      Instance response = instancesClient.get(PROJECT_ID, ZONE, instanceName);
      return response.getStatus();
    }
  }

  @Before
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @After
  public void afterEach() {
    stdOut = null;
    System.setOut(null);
  }

  @Test
  public void testCreateInstance() throws IOException {
    // Check if the instance was successfully created during the setup.
    Status response = getInstanceStatus(MACHINE_NAME);
    Assert.assertSame(response, Status.RUNNING);
  }

  @Test
  public void testCreateBulkInstance() throws IOException {
    // Check if the instance was successfully created during the setup.
    Status response = getInstanceStatus(MACHINE_NAME);
    Assert.assertSame(response, Status.RUNNING);
  }

  @Test
  public void testCreateEncryptedInstance() throws IOException {
    // Check if the instance was successfully created during the setup.
    Status response = getInstanceStatus(MACHINE_NAME_ENCRYPTED);
    Assert.assertSame(response, Status.RUNNING);
  }

  @Test
  public void testCreatePublicImage() throws IOException {
    // Check if the instance was successfully created during the setup.
    Status response = getInstanceStatus(MACHINE_NAME_PUBLIC_IMAGE);
    Assert.assertSame(response, Status.RUNNING);
  }

  @Test
  public void testCreateCustomImage() throws IOException {
    // Check if the instance was successfully created during the setup.
    Status response = getInstanceStatus(MACHINE_NAME_CUSTOM_IMAGE);
    Assert.assertSame(response, Status.RUNNING);
  }

  @Test
  public void testCreateAdditionalDisk() throws IOException {
    // Check if the instance was successfully created during the setup.
    Status response = getInstanceStatus(MACHINE_NAME_ADDITIONAL_DISK);
    Assert.assertSame(response, Status.RUNNING);
  }

  @Test
  public void testCreateFromSnapshot() throws IOException {
    // Check if the instance was successfully created during the setup.
    Status response = getInstanceStatus(MACHINE_NAME_SNAPSHOT);
    Assert.assertSame(response, Status.RUNNING);
  }

  @Test
  public void testCreateFromSnapshotAdditional() throws IOException {
    // Check if the instance was successfully created during the setup.
    Status response = getInstanceStatus(MACHINE_NAME_SNAPSHOT_ADDITIONAL);
    Assert.assertSame(response, Status.RUNNING);
  }

  @Test
  public void testCreateInSubnetwork() throws IOException {
    // Check if the instance was successfully created during the setup.
    Status response = getInstanceStatus(MACHINE_NAME_SUBNETWORK);
    Assert.assertSame(response, Status.RUNNING);
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
  public void testDeleteInstance() throws IOException, InterruptedException, ExecutionException {
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_DELETE);
    assertThat(stdOut.toString()).contains("Operation Status: DONE");
  }

  @Test
  public void testWaitForOperation() throws IOException, InterruptedException {
    // Construct a delete request and get the operation instance.
    InstancesClient instancesClient = InstancesClient.create();
    Operation operation = instancesClient.delete(PROJECT_ID, ZONE, MACHINE_NAME_WAIT_FOR_OP);

    // Pass the operation ID and wait for it to complete.
    compute.WaitForOperation.waitForOperation(PROJECT_ID, operation);
    assertThat(stdOut.toString().contains("Operation Status: DONE"));
  }

  @Test
  public void testSetUsageBucketExportCustomPrefix() throws IOException, InterruptedException {
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
  public void testCreateFirewallRule() throws IOException {
    // Assert that firewall rule has been created as part of the setup.
    compute.GetFirewallRule.getFirewallRule(PROJECT_ID, FIREWALL_RULE_CREATE);
    assertThat(stdOut.toString()).contains(FIREWALL_RULE_CREATE);
  }

  @Test
  public void testInstanceOperations()
      throws IOException, ExecutionException, InterruptedException {
    Assert.assertSame(getInstanceStatus(MACHINE_NAME), Status.RUNNING);

    // Stopping the instance.
    StopInstance.stopInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    // Wait for the operation to complete. Setting timeout to 3 mins.
    LocalDateTime endTime = LocalDateTime.now().plusMinutes(3);
    while (getInstanceStatus(MACHINE_NAME) == Status.STOPPING
        && LocalDateTime.now().isBefore(endTime)) {
      TimeUnit.SECONDS.sleep(5);
    }
    Assert.assertSame(getInstanceStatus(MACHINE_NAME), Status.TERMINATED);

    // Starting the instance.
    StartInstance.startInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    // Wait for the operation to complete. Setting timeout to 3 mins.
    endTime = LocalDateTime.now().plusMinutes(3);
    while (getInstanceStatus(MACHINE_NAME) != Status.RUNNING
        && LocalDateTime.now().isBefore(endTime)) {
      TimeUnit.SECONDS.sleep(5);
    }
    Assert.assertSame(getInstanceStatus(MACHINE_NAME), Status.RUNNING);
  }

  @Test
  public void testEncryptedInstanceOperations()
      throws IOException, ExecutionException, InterruptedException {
    Assert.assertSame(getInstanceStatus(MACHINE_NAME_ENCRYPTED), Status.RUNNING);

    // Stopping the encrypted instance.
    StopInstance.stopInstance(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED);
    // Wait for the operation to complete. Setting timeout to 3 mins.
    LocalDateTime endTime = LocalDateTime.now().plusMinutes(3);
    while (getInstanceStatus(MACHINE_NAME_ENCRYPTED) == Status.STOPPING
        && LocalDateTime.now().isBefore(endTime)) {
      TimeUnit.SECONDS.sleep(5);
    }
    Assert.assertSame(getInstanceStatus(MACHINE_NAME_ENCRYPTED), Status.TERMINATED);

    // Starting the encrypted instance.
    StartEncryptedInstance
        .startEncryptedInstance(PROJECT_ID, ZONE, MACHINE_NAME_ENCRYPTED, RAW_KEY);
    // Wait for the operation to complete. Setting timeout to 3 mins.
    endTime = LocalDateTime.now().plusMinutes(3);
    while (getInstanceStatus(MACHINE_NAME_ENCRYPTED) != Status.RUNNING
        && LocalDateTime.now().isBefore(endTime)) {
      TimeUnit.SECONDS.sleep(5);
    }
    Assert.assertSame(getInstanceStatus(MACHINE_NAME_ENCRYPTED), Status.RUNNING);
  }

}
