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

import com.google.cloud.compute.v1.FirewallsClient;
import com.google.cloud.compute.v1.Instance;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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
  private static String BUCKET_NAME;
  private static String IMAGE_NAME;
  private static String FIREWALL_RULE_CREATE;
  private static String FIREWALL_RULE_DELETE;

  private ByteArrayOutputStream stdOut;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeClass
  public static void setUp() throws IOException, InterruptedException {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    ZONE = "us-central1-a";
    MACHINE_NAME = "my-new-test-instance" + UUID.randomUUID().toString();
    MACHINE_NAME_DELETE = "my-new-test-instance" + UUID.randomUUID().toString();
    MACHINE_NAME_LIST_INSTANCE = "my-new-test-instance" + UUID.randomUUID().toString();
    MACHINE_NAME_WAIT_FOR_OP = "my-new-test-instance" + UUID.randomUUID().toString();
    BUCKET_NAME = "my-new-test-bucket" + UUID.randomUUID().toString();
    IMAGE_NAME = "windows-sql-cloud";
    FIREWALL_RULE_CREATE = "firewall-rule-" + UUID.randomUUID().toString();
    FIREWALL_RULE_DELETE = "firewall-rule-" + UUID.randomUUID().toString();

    compute.CreateInstance.createInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    compute.CreateInstance.createInstance(PROJECT_ID, ZONE, MACHINE_NAME_DELETE);
    compute.CreateInstance.createInstance(PROJECT_ID, ZONE, MACHINE_NAME_LIST_INSTANCE);
    compute.CreateInstance.createInstance(PROJECT_ID, ZONE, MACHINE_NAME_WAIT_FOR_OP);
    TimeUnit.SECONDS.sleep(10);
    compute.CreateFirewallRule.createFirewall(PROJECT_ID, FIREWALL_RULE_CREATE);
    compute.CreateFirewallRule.createFirewall(PROJECT_ID, FIREWALL_RULE_DELETE);

    // Create a Google Cloud Storage bucket for UsageReports
    Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    storage.create(BucketInfo.of(BUCKET_NAME));
  }


  @AfterClass
  public static void cleanup() throws IOException, InterruptedException {
    // Delete all instances created for testing.
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    compute.DeleteFirewallRule.deleteFirewallRule(PROJECT_ID, FIREWALL_RULE_CREATE);
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    compute.DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_LIST_INSTANCE);

    // Delete the Google Cloud Storage bucket created for usage reports.
    Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    Bucket bucket = storage.get(BUCKET_NAME);
    bucket.delete();

    stdOut.close();
    System.setOut(null);
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
    try (InstancesClient instancesClient = InstancesClient.create()) {
      Instance response = instancesClient.get(PROJECT_ID, ZONE, MACHINE_NAME);
      Assert.assertNotNull(response);
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
  public void testDeleteInstance() throws IOException, InterruptedException {
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
    ListImages.listImages(IMAGE_NAME);
    int imageCount = Integer.parseInt(stdOut.toString().split(":")[1].trim());
    Assert.assertTrue(imageCount > 2);
  }

  @Test
  public void testListImagesByPage() throws IOException {
    // ================= Paginated list of images ================
    ListImages.listImagesByPage(IMAGE_NAME, 2);
    Assert.assertTrue(stdOut.toString().contains("Page Number: 1"));
  }

  @Test
  public void testCreateFirewallRule() throws IOException {
    // Assert that firewall rule has been created as part of the setup.
    compute.GetFirewallRule.getFirewallRule(PROJECT_ID, FIREWALL_RULE_CREATE);
    compute.GetFirewallRule.getFirewallRule(PROJECT_ID, FIREWALL_RULE_DELETE);
    assertThat(stdOut.toString()).contains(FIREWALL_RULE_CREATE);
    assertThat(stdOut.toString()).contains(FIREWALL_RULE_DELETE);
  }

  @Test
  public void testListFirewallRules() throws IOException {
    compute.ListFirewallRules.listFirewallRules(PROJECT_ID);
    assertThat(stdOut.toString()).contains(FIREWALL_RULE_CREATE);
  }

  @Test
  public void testPatchFirewallRule() throws IOException, InterruptedException {
    try(FirewallsClient client = FirewallsClient.create()) {
      Assert.assertTrue(client.get(PROJECT_ID, FIREWALL_RULE_CREATE).getPriority() == 1000);
      compute.PatchFirewallRule.patchFirewallPriority(PROJECT_ID, FIREWALL_RULE_CREATE, 500);
      TimeUnit.SECONDS.sleep(5);
      Assert.assertTrue(client.get(PROJECT_ID, FIREWALL_RULE_CREATE).getPriority() == 500);
    }
  }

}
