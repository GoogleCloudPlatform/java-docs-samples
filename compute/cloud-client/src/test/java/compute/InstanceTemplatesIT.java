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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class InstanceTemplatesIT {


  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static String TEMPLATE_NAME;
  private static String TEMPLATE_NAME_FROM_INSTANCE;
  private static String TEMPLATE_NAME_WITH_SUBNET;
  private static String ZONE;
  private static String MACHINE_NAME;

  private ByteArrayOutputStream stdOut;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeClass
  public static void setup() throws IOException, ExecutionException, InterruptedException {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    TEMPLATE_NAME = "template-name-" + UUID.randomUUID();
    ZONE = "us-central1-a";
    MACHINE_NAME = "my-new-test-instance" + UUID.randomUUID();
    TEMPLATE_NAME_FROM_INSTANCE = "my-new-test-instance" + UUID.randomUUID();
    TEMPLATE_NAME_WITH_SUBNET = "my-new-test-instance" + UUID.randomUUID();

    CreateInstanceTemplate.createInstanceTemplate(PROJECT_ID, TEMPLATE_NAME);
    CreateInstance.createInstance(PROJECT_ID, ZONE, MACHINE_NAME);
    TimeUnit.SECONDS.sleep(10);
    CreateTemplateFromInstance.createTemplateFromInstance(PROJECT_ID, TEMPLATE_NAME_FROM_INSTANCE,
        getInstance(ZONE, MACHINE_NAME).getSelfLink());
    CreateTemplateWithSubnet.createTemplateWithSubnet(PROJECT_ID, "global/networks/default",
        "regions/asia-east1/subnetworks/default", TEMPLATE_NAME_WITH_SUBNET);
    TimeUnit.SECONDS.sleep(10);
  }

  @AfterClass
  public static void cleanup() throws IOException, ExecutionException, InterruptedException {
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    DeleteInstanceTemplate.deleteInstanceTemplate(PROJECT_ID, TEMPLATE_NAME);
    DeleteInstanceTemplate.deleteInstanceTemplate(PROJECT_ID, TEMPLATE_NAME_FROM_INSTANCE);
    DeleteInstanceTemplate.deleteInstanceTemplate(PROJECT_ID, TEMPLATE_NAME_WITH_SUBNET);
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
