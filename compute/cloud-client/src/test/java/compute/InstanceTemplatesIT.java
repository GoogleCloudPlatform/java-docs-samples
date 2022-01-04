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

import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstanceTemplate;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.InstancesScopedList;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
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
public class InstanceTemplatesIT {


  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String DEFAULT_REGION = "us-central1";
  private static final String DEFAULT_ZONE = DEFAULT_REGION + "-a";
  private static String TEMPLATE_NAME;
  private static String TEMPLATE_NAME_WITH_DISK;
  private static String TEMPLATE_NAME_FROM_INSTANCE;
  private static String TEMPLATE_NAME_WITH_SUBNET;
  private static String MACHINE_NAME_CR;
  private static String MACHINE_NAME_CR_TEMPLATE;
  private static String MACHINE_NAME_CR_TEMPLATE_OR;

  private ByteArrayOutputStream stdOut;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeClass
  public static void setup() throws IOException, ExecutionException, InterruptedException {
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    String templateUUID = UUID.randomUUID().toString();
    TEMPLATE_NAME = "test-csam-template-" + templateUUID;
    TEMPLATE_NAME_WITH_DISK = "test-csam-template-disk-" + templateUUID;
    TEMPLATE_NAME_FROM_INSTANCE = "test-csam-template-inst-" + templateUUID;
    TEMPLATE_NAME_WITH_SUBNET = "test-csam-template-snet-" + templateUUID;
    String instanceUUID = UUID.randomUUID().toString();
    MACHINE_NAME_CR = "test-csam-instance" + instanceUUID;
    MACHINE_NAME_CR_TEMPLATE = "test-csam-inst-template-" + instanceUUID;
    MACHINE_NAME_CR_TEMPLATE_OR =
        "test-csam-inst-temp-or-" + instanceUUID;

    // Check for resources created >24hours which haven't been deleted in the project.
    cleanUpExistingTestResources("test-csam-");

    // Create templates.
    CreateInstanceTemplate.createInstanceTemplate(PROJECT_ID, TEMPLATE_NAME);
    assertThat(stdOut.toString()).contains("Instance Template Operation Status " + TEMPLATE_NAME);
    CreateInstance.createInstance(PROJECT_ID, DEFAULT_ZONE, MACHINE_NAME_CR);
    TimeUnit.SECONDS.sleep(10);
    CreateTemplateFromInstance.createTemplateFromInstance(PROJECT_ID, TEMPLATE_NAME_FROM_INSTANCE,
        getInstance(DEFAULT_ZONE, MACHINE_NAME_CR).getSelfLink());
    assertThat(stdOut.toString())
        .contains("Instance Template creation operation status " + TEMPLATE_NAME_FROM_INSTANCE);
    CreateTemplateWithSubnet.createTemplateWithSubnet(PROJECT_ID, "global/networks/default",
        String.format("regions/%s/subnetworks/default", DEFAULT_REGION), TEMPLATE_NAME_WITH_SUBNET);
    assertThat(stdOut.toString())
        .contains("Template creation from subnet operation status " + TEMPLATE_NAME_WITH_SUBNET);
    TimeUnit.SECONDS.sleep(10);

    // Create instances.
    CreateInstanceFromTemplate.createInstanceFromTemplate(PROJECT_ID, DEFAULT_ZONE,
        MACHINE_NAME_CR_TEMPLATE,
        "global/instanceTemplates/" + TEMPLATE_NAME);
    assertThat(stdOut.toString())
        .contains("Instance creation from template: Operation Status " + MACHINE_NAME_CR_TEMPLATE);
    CreateInstanceTemplate.createInstanceTemplateWithDiskType(PROJECT_ID, TEMPLATE_NAME_WITH_DISK);
    CreateInstanceFromTemplateWithOverrides
        .createInstanceFromTemplateWithOverrides(PROJECT_ID, DEFAULT_ZONE,
            MACHINE_NAME_CR_TEMPLATE_OR,
            TEMPLATE_NAME_WITH_DISK);
    assertThat(stdOut.toString()).contains(
        "Instance creation from template with overrides: Operation Status "
            + MACHINE_NAME_CR_TEMPLATE_OR);
    Assert.assertEquals(getInstance(DEFAULT_ZONE, MACHINE_NAME_CR_TEMPLATE_OR).getDisksCount(), 2);
    stdOut.close();
    System.setOut(null);
  }

  @AfterClass
  public static void cleanup() throws IOException, ExecutionException, InterruptedException {
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    // Delete instances.
    DeleteInstance.deleteInstance(PROJECT_ID, DEFAULT_ZONE, MACHINE_NAME_CR);
    DeleteInstance.deleteInstance(PROJECT_ID, DEFAULT_ZONE, MACHINE_NAME_CR_TEMPLATE);
    DeleteInstance.deleteInstance(PROJECT_ID, DEFAULT_ZONE, MACHINE_NAME_CR_TEMPLATE_OR);
    // Delete instance templates.
    DeleteInstanceTemplate.deleteInstanceTemplate(PROJECT_ID, TEMPLATE_NAME);
    assertThat(stdOut.toString())
        .contains("Instance template deletion operation status for " + TEMPLATE_NAME);
    DeleteInstanceTemplate.deleteInstanceTemplate(PROJECT_ID, TEMPLATE_NAME_FROM_INSTANCE);
    assertThat(stdOut.toString())
        .contains("Instance template deletion operation status for " + TEMPLATE_NAME_FROM_INSTANCE);
    DeleteInstanceTemplate.deleteInstanceTemplate(PROJECT_ID, TEMPLATE_NAME_WITH_SUBNET);
    assertThat(stdOut.toString())
        .contains("Instance template deletion operation status for " + TEMPLATE_NAME_WITH_SUBNET);
    stdOut.close();
    System.setOut(null);
  }

  // Cleans existing test resources if any.
  // If the project contains too many instances, use "filter" when listing resources
  // and delete the listed resources based on the timestamp.
  public static void cleanUpExistingTestResources(String prefixToDelete)
      throws IOException, ExecutionException, InterruptedException {
    boolean isBefore24Hours = false;

    // Delete templates which starts with the given prefixToDelete and has creation timestamp >24 hours.
    for (InstanceTemplate template : ListInstanceTemplates.listInstanceTemplates(PROJECT_ID)
        .iterateAll()) {
      if(!template.hasCreationTimestamp()) {
        continue;
      }
      isBefore24Hours = Instant.parse(template.getCreationTimestamp())
          .isBefore(Instant.now().minus(24, ChronoUnit.HOURS));
      if (template.getName().contains(prefixToDelete) && isBefore24Hours) {
        DeleteInstanceTemplate.deleteInstanceTemplate(PROJECT_ID, template.getName());
      }
    }

    // Delete instances which starts with the given prefixToDelete and has creation timestamp >24 hours.
    for (Entry<String, InstancesScopedList> instanceGroup : ListAllInstances.listAllInstances(
        PROJECT_ID).iterateAll()) {
      String instanceZone = instanceGroup.getKey();
      for (Instance instance : instanceGroup.getValue().getInstancesList()) {
        if(!instance.hasCreationTimestamp()) {
          continue;
        }
        isBefore24Hours = Instant.parse(instance.getCreationTimestamp())
            .isBefore(Instant.now().minus(24, ChronoUnit.HOURS));
        if (instance.getName().contains(prefixToDelete) && isBefore24Hours) {
          DeleteInstance.deleteInstance(PROJECT_ID, instanceZone, instance.getName());
        }
      }
    }
  }

  public static Instance getInstance(String zone, String instanceName) throws IOException {
    try (InstancesClient instancesClient = InstancesClient.create()) {
      return instancesClient.get(PROJECT_ID, zone, instanceName);
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
  public void testGetInstanceTemplate() throws IOException {
    GetInstanceTemplate.getInstanceTemplate(PROJECT_ID, TEMPLATE_NAME);
    assertThat(stdOut.toString()).contains(TEMPLATE_NAME);
    GetInstanceTemplate.getInstanceTemplate(PROJECT_ID, TEMPLATE_NAME_FROM_INSTANCE);
    assertThat(stdOut.toString()).contains(TEMPLATE_NAME_FROM_INSTANCE);
    GetInstanceTemplate.getInstanceTemplate(PROJECT_ID, TEMPLATE_NAME_WITH_SUBNET);
    assertThat(stdOut.toString()).contains(TEMPLATE_NAME_WITH_SUBNET);
  }

  @Test
  public void testListInstanceTemplates() throws IOException {
    ListInstanceTemplates.listInstanceTemplates(PROJECT_ID);
    assertThat(stdOut.toString()).contains(TEMPLATE_NAME);
    assertThat(stdOut.toString()).contains(TEMPLATE_NAME_FROM_INSTANCE);
    assertThat(stdOut.toString()).contains(TEMPLATE_NAME_WITH_SUBNET);
  }

}