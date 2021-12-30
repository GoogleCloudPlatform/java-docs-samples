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
import com.google.cloud.compute.v1.InstancesClient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
  private static String TEMPLATE_NAME;
  private static String TEMPLATE_NAME_WITH_DISK;
  private static String ZONE;
  private static String MACHINE_NAME;
  private static String MACHINE_NAME_2;
  private static String MACHINE_NAME_3;

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

    TEMPLATE_NAME = "template-name-" + UUID.randomUUID();
    ZONE = "us-central1-a";
    MACHINE_NAME = "my-new-test-instance" + UUID.randomUUID();
    MACHINE_NAME_2 = "my-new-test-instance" + UUID.randomUUID();
    MACHINE_NAME_3 = "my-new-test-instance" + UUID.randomUUID();
    TEMPLATE_NAME_WITH_DISK = "my-new-test-instance" + UUID.randomUUID();

    // Create templates.
    CreateInstanceTemplate.createInstanceTemplate(PROJECT_ID, TEMPLATE_NAME);
    assertThat(stdOut.toString()).contains("Instance Template Operation Status " + TEMPLATE_NAME);
    CreateInstance.createInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    TimeUnit.SECONDS.sleep(10);

    // Create instances.
    CreateInstanceFromTemplate.createInstanceFromTemplate(PROJECT_ID, ZONE, MACHINE_NAME_2,
        "global/instanceTemplates/" + TEMPLATE_NAME);
    assertThat(stdOut.toString())
        .contains("Instance creation from template: Operation Status " + MACHINE_NAME_2);
    CreateInstanceTemplate.createInstanceTemplateWithDiskType(PROJECT_ID, TEMPLATE_NAME_WITH_DISK);
    CreateInstanceFromTemplateWithOverrides
        .createInstanceFromTemplateWithOverrides(PROJECT_ID, ZONE, MACHINE_NAME_3,
            TEMPLATE_NAME_WITH_DISK);
    assertThat(stdOut.toString()).contains(
        "Instance creation from template with overrides: Operation Status " + MACHINE_NAME_3);
    Assert.assertEquals(getInstance(ZONE, MACHINE_NAME_3).getDisksCount(), 2);
    stdOut.close();
    System.setOut(null);
  }

  @AfterClass
  public static void cleanup() throws IOException, ExecutionException, InterruptedException {
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    // Delete instances.
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_2);
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_3);
    // Delete instance templates.
    DeleteInstanceTemplate.deleteInstanceTemplate(PROJECT_ID, TEMPLATE_NAME);
    assertThat(stdOut.toString())
        .contains("Instance template deletion operation status for " + TEMPLATE_NAME);
    stdOut.close();
    System.setOut(null);
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
  }

  @Test
  public void testListInstanceTemplates() throws IOException {
    ListInstanceTemplates.listInstanceTemplates(PROJECT_ID);
    assertThat(stdOut.toString()).contains(TEMPLATE_NAME);
  }

}