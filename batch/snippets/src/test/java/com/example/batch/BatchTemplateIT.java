// Copyright 2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.batch;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.compute.v1.AccessConfig;
import com.google.cloud.compute.v1.AccessConfig.NetworkTier;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.DeleteInstanceTemplateRequest;
import com.google.cloud.compute.v1.InsertInstanceTemplateRequest;
import com.google.cloud.compute.v1.InstanceProperties;
import com.google.cloud.compute.v1.InstanceTemplate;
import com.google.cloud.compute.v1.InstanceTemplatesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Scheduling;
import com.google.cloud.compute.v1.Scheduling.OnHostMaintenance;
import com.google.cloud.compute.v1.Scheduling.ProvisioningModel;
import com.google.cloud.compute.v1.ServiceAccount;
import com.google.cloud.resourcemanager.v3.ProjectsClient;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BatchTemplateIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION = "us-central1";
  private static final int MAX_ATTEMPT_COUNT = 3;
  private static final int INITIAL_BACKOFF_MILLIS = 120000; // 2 minutes
  private static String PROJECT_NUMBER;
  private static String SCRIPT_JOB_NAME;
  private static InstanceTemplate INSTANCE_TEMPLATE;

  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(
      MAX_ATTEMPT_COUNT,
      INITIAL_BACKOFF_MILLIS);
  private ByteArrayOutputStream stdOut;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName))
        .isNotEmpty();
  }

  @BeforeClass
  public static void setUp()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    try (PrintStream out = System.out) {
      ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
      System.setOut(new PrintStream(stdOut));
      requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
      requireEnvVar("GOOGLE_CLOUD_PROJECT");

      // Get project number from project id.
      try (ProjectsClient projectsClient = ProjectsClient.create()) {
        PROJECT_NUMBER = projectsClient.getProject(String.format("projects/%s", PROJECT_ID))
            .getName().split("/")[1];
      }
      String uuid = String.valueOf(UUID.randomUUID());
      SCRIPT_JOB_NAME = "test-job-template-" + uuid;

      // Delete stale instance templates.
      Util.cleanUpExistingInstanceTemplates("test-job-template-", PROJECT_ID);
      // Delete existing stale jobs if any.
      try {
        DeleteJob.deleteJob(PROJECT_ID, REGION, SCRIPT_JOB_NAME);
      } catch (ExecutionException e) {
        if (!e.getMessage().contains("NOT_FOUND")) {
          throw e;
        }
        // System.out.println("Do nothing");
      }

      // Create instance templates.
      INSTANCE_TEMPLATE = createInstanceTemplate();
      TimeUnit.SECONDS.sleep(10);

      // Create job with template.
      CreateWithTemplate.createWithTemplate(PROJECT_ID, REGION, SCRIPT_JOB_NAME,
          INSTANCE_TEMPLATE.getSelfLink());
      assertThat(stdOut.toString()).contains("Successfully created the job: ");

      stdOut.close();
      System.setOut(out);
    }
  }

  @AfterClass
  public static void cleanup()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    try (PrintStream out = System.out) {
      ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
      System.setOut(new PrintStream(stdOut));

      deleteInstanceTemplate();
      DeleteJob.deleteJob(PROJECT_ID, REGION, SCRIPT_JOB_NAME);

      stdOut.close();
      System.setOut(out);
    }
  }

  // Create a new instance template with the provided name and a specific
  // instance configuration.
  public static InstanceTemplate createInstanceTemplate()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (InstanceTemplatesClient instanceTemplatesClient = InstanceTemplatesClient.create()) {

      String machineType = "e2-standard-16";
      String sourceImage = "projects/ubuntu-os-cloud/global/images/family/ubuntu-2204-lts";

      // The template describes the size and source image of the boot disk
      // to attach to the instance.
      AttachedDisk attachedDisk = AttachedDisk.newBuilder()
          .setInitializeParams(AttachedDiskInitializeParams.newBuilder()
              .setSourceImage(sourceImage)
              .setDiskType("pd-balanced")
              .setDiskSizeGb(25).build())
          .setAutoDelete(true)
          .setBoot(true).build();

      // The template connects the instance to the `default` network,
      // without specifying a subnetwork.
      NetworkInterface networkInterface = NetworkInterface.newBuilder()
          .setName("global/networks/default")
          // The template lets the instance use an external IP address.
          .addAccessConfigs(AccessConfig.newBuilder()
              .setName("External NAT")
              .setType(AccessConfig.Type.ONE_TO_ONE_NAT.toString())
              .setNetworkTier(NetworkTier.PREMIUM.toString()).build()).build();

      Scheduling scheduling = Scheduling.newBuilder()
          .setOnHostMaintenance(OnHostMaintenance.MIGRATE.name())
          .setProvisioningModel(ProvisioningModel.STANDARD.name())
          .setAutomaticRestart(true)
          .build();

      ServiceAccount serviceAccount = ServiceAccount.newBuilder()
          .setEmail(String.format("%s-compute@developer.gserviceaccount.com", PROJECT_NUMBER))
          .addAllScopes(Arrays.asList(
              "https://www.googleapis.com/auth/devstorage.read_only",
              "https://www.googleapis.com/auth/logging.write",
              "https://www.googleapis.com/auth/monitoring.write",
              "https://www.googleapis.com/auth/servicecontrol",
              "https://www.googleapis.com/auth/service.management.readonly",
              "https://www.googleapis.com/auth/trace.append"))
          .build();

      InstanceProperties instanceProperties = InstanceProperties.newBuilder()
          .addDisks(attachedDisk)
          .setMachineType(machineType)
          .addNetworkInterfaces(networkInterface)
          .setScheduling(scheduling)
          .addServiceAccounts(serviceAccount)
          .build();

      String templateName = "template-name-" + UUID.randomUUID();
      InsertInstanceTemplateRequest insertInstanceTemplateRequest = InsertInstanceTemplateRequest
          .newBuilder()
          .setProject(PROJECT_ID)
          .setInstanceTemplateResource(InstanceTemplate.newBuilder()
              .setName(templateName)
              .setProperties(instanceProperties).build()).build();

      // Create the Instance Template.
      Operation response = instanceTemplatesClient.insertAsync(insertInstanceTemplateRequest)
          .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Instance Template creation failed ! ! " + response);
        return null;
      }
      return instanceTemplatesClient.get(PROJECT_ID, templateName);
    }
  }

  private static void deleteInstanceTemplate()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (InstanceTemplatesClient instanceTemplatesClient = InstanceTemplatesClient.create()) {
      instanceTemplatesClient.deleteCallable().futureCall(
          DeleteInstanceTemplateRequest.newBuilder()
              .setProject(PROJECT_ID)
              .setInstanceTemplate(INSTANCE_TEMPLATE.getName())
              .build()).get(3, TimeUnit.MINUTES);
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
  public void testCreateWithTemplate()
      throws IOException, InterruptedException {
    Util.waitForJobCompletion(Util.getJob(PROJECT_ID, REGION, SCRIPT_JOB_NAME));
    assertThat(stdOut.toString()).contains("Job completed");
  }
}
