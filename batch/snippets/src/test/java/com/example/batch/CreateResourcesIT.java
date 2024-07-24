// Copyright 2024 Google LLC
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

import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.batch.v1.AllocationPolicy;
import com.google.cloud.batch.v1.Job;
import com.google.cloud.batch.v1.JobNotification.Type;
import com.google.cloud.batch.v1.TaskStatus.State;
import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.InsertDiskRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CreateResourcesIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION = "us-central1";
  private static final String ZONE = "us-central1-a";
  private static final int LOCAL_SSD_SIZE = 375;
  private static final String SERVICE_ACCOUNT_JOB = "test-job-sa-"
          + UUID.randomUUID().toString().substring(0, 7);
  private static final String SECRET_MANAGER_JOB = "test-job-sm-"
          + UUID.randomUUID().toString().substring(0, 7);
  private static final String GPU_JOB = "test-job-gpu-"
          + UUID.randomUUID().toString().substring(0, 7);
  private static final String GPU_JOB_N1 = "test-job-gpun1-"
          + UUID.randomUUID().toString().substring(0, 7);
  private static final String LOCAL_SSD_JOB = "test-job-lssd-"
          + UUID.randomUUID().toString().substring(0, 7);
  private static final String PERSISTENT_DISK_JOB = "test-job-pd-"
          + UUID.randomUUID().toString().substring(0, 7);
  private static final String NOTIFICATION_NAME = "test-job-notif-"
          + UUID.randomUUID().toString().substring(0, 7);
  private static final String CUSTOM_EVENT_NAME = "test-job-event-"
          + UUID.randomUUID().toString().substring(0, 7);
  private static final String BATCH_LABEL_JOB = "test-job-label"
      + UUID.randomUUID().toString().substring(0, 7);
  private static final String CUSTOM_NETWORK_NAME = "test-job-network"
      + UUID.randomUUID().toString().substring(0, 7);
  private static final String JOB_ALLOCATION_POLICY_LABEL = "test-job-allocation-label"
      + UUID.randomUUID().toString().substring(0, 7);
  private static final String BATCH_RUNNABLE_LABEL = "test-runnable-label"
      + UUID.randomUUID().toString().substring(0, 7);
  private static final String LOCAL_SSD_NAME = "test-disk"
          + UUID.randomUUID().toString().substring(0, 7);
  private static final String PERSISTENT_DISK_NAME = "test-disk"
          + UUID.randomUUID().toString().substring(0, 7);
  private static final String NEW_PERSISTENT_DISK_NAME = "test-disk"
          + UUID.randomUUID().toString().substring(0, 7);
  private static final List<Job> ACTIVE_JOBS = new ArrayList<>();
  private static final String NFS_PATH = "test-disk";
  private static final String NFS_IP_ADDRESS = "test123";
  private static final String NFS_JOB_NAME = "test-job"
          + UUID.randomUUID().toString().substring(0, 7);

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
            .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeClass
  public static void setUp() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @AfterClass
  public static void cleanUp() {
    for (Job job : ACTIVE_JOBS) {
      try {
        Util.waitForJobCompletion(job);
      } catch (IOException | InterruptedException e) {
        System.err.println(e.getMessage());
      }
    }
    try (DisksClient client = DisksClient.create()) {
      client.deleteAsync(PROJECT_ID, ZONE, PERSISTENT_DISK_NAME).get(60, TimeUnit.SECONDS);
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }

    safeDeleteJob(SERVICE_ACCOUNT_JOB);
    safeDeleteJob(SECRET_MANAGER_JOB);
    safeDeleteJob(GPU_JOB);
    safeDeleteJob(GPU_JOB_N1);
    safeDeleteJob(LOCAL_SSD_JOB);
    safeDeleteJob(PERSISTENT_DISK_JOB);
    safeDeleteJob(NOTIFICATION_NAME);
    safeDeleteJob(CUSTOM_EVENT_NAME);
    safeDeleteJob(NFS_JOB_NAME);
    safeDeleteJob(BATCH_LABEL_JOB);
    safeDeleteJob(CUSTOM_NETWORK_NAME);
    safeDeleteJob(JOB_ALLOCATION_POLICY_LABEL);
    safeDeleteJob(BATCH_RUNNABLE_LABEL);
  }

  private static void safeDeleteJob(String jobName) {
    try {
      DeleteJob.deleteJob(PROJECT_ID, REGION, jobName);
    } catch (IOException | ExecutionException | InterruptedException | TimeoutException e) {
      System.err.println(e.getMessage());
    }
  }

  @Test
  public void createBatchCustomServiceAccountTest()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Job job = CreateBatchUsingServiceAccount
            .createBatchUsingServiceAccount(PROJECT_ID, REGION, SERVICE_ACCOUNT_JOB, null);

    Assert.assertNotNull(job);
    ACTIVE_JOBS.add(job);

    Assert.assertTrue(job.getName().contains(SERVICE_ACCOUNT_JOB));
    Assert.assertNotNull(job.getAllocationPolicy().getServiceAccount().getEmail());
  }

  @Test
  public void createBatchUsingSecretManager()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String variableName = "uuui";
    Job job = CreateBatchUsingSecretManager
            .createBatchUsingSecretManager(PROJECT_ID, REGION, SECRET_MANAGER_JOB,
                    variableName, "secretName", "v1");

    Assert.assertNotNull(job);
    ACTIVE_JOBS.add(job);

    Assert.assertTrue(job.getName().contains(SECRET_MANAGER_JOB));
    Assert.assertTrue(job.getTaskGroupsList().stream().anyMatch(taskGroup
            -> taskGroup.getTaskSpec().getEnvironment().containsSecretVariables(variableName)));
  }

  @Test
  public void createGpuJobTest()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String machineType = "g2-standard-4";
    Job job = CreateGpuJob
            .createGpuJob(PROJECT_ID, REGION, GPU_JOB, true, machineType);

    Assert.assertNotNull(job);
    ACTIVE_JOBS.add(job);

    Assert.assertTrue(job.getName().contains(GPU_JOB));
    Assert.assertTrue(job.getAllocationPolicy().getInstancesList().stream().anyMatch(instance
        -> instance.getInstallGpuDrivers()));
    Assert.assertTrue(job.getAllocationPolicy().getInstancesList().stream().anyMatch(instance
        -> instance.getPolicy().getMachineType().contains(machineType)));
  }

  @Test
  public void createGpuJobN1Test()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String gpuType = "nvidia-tesla-t4";
    int count = 2;
    Job job = CreateGpuJobN1
            .createGpuJob(PROJECT_ID, REGION, GPU_JOB_N1, true, gpuType, count);

    Assert.assertNotNull(job);
    ACTIVE_JOBS.add(job);

    Assert.assertTrue(job.getName().contains(GPU_JOB_N1));
    Assert.assertTrue(job.getAllocationPolicy().getInstancesList().stream().anyMatch(instance
        -> instance.getInstallGpuDrivers() && instance.getPolicy().getAcceleratorsList().stream()
            .anyMatch(accelerator
                -> accelerator.getType().contains(gpuType) && accelerator.getCount() == count)));
  }

  @Test
  public void createLocalSsdJobTest()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String type = "c3d-standard-8-lssd";
    Job job = CreateLocalSsdJob
            .createLocalSsdJob(PROJECT_ID, REGION, LOCAL_SSD_JOB, LOCAL_SSD_NAME,
                LOCAL_SSD_SIZE, type);

    Assert.assertNotNull(job);
    ACTIVE_JOBS.add(job);

    Assert.assertTrue(job.getName().contains(LOCAL_SSD_JOB));
    Assert.assertTrue(job.getAllocationPolicy().getInstancesList().stream()
            .anyMatch(instance -> instance.getPolicy().getMachineType().contains(type)
                && instance.getPolicy().getDisksList().stream().anyMatch(attachedDisk
                    -> attachedDisk.getDeviceName().contains(LOCAL_SSD_NAME))));
  }

  @Test
  public void createPersistentDiskJobTest()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String diskType = String.format("zones/%s/diskTypes/pd-balanced", ZONE);
    createEmptyDisk(PROJECT_ID, ZONE, PERSISTENT_DISK_NAME, diskType, 10);

    Job job = CreatePersistentDiskJob
            .createPersistentDiskJob(PROJECT_ID, REGION, PERSISTENT_DISK_JOB,
                NEW_PERSISTENT_DISK_NAME, 10, PERSISTENT_DISK_NAME, "zones/" + ZONE, diskType);

    Assert.assertNotNull(job);
    ACTIVE_JOBS.add(job);

    Assert.assertTrue(job.getName().contains(PERSISTENT_DISK_JOB));

    Assert.assertTrue(job.getAllocationPolicy().getInstancesList().stream()
            .anyMatch(policy -> policy.getPolicy().getDisksList().stream()
                    .anyMatch(attachedDisk
                            -> attachedDisk.getDeviceName().contains(PERSISTENT_DISK_NAME))));

    Assert.assertTrue(job.getAllocationPolicy().getInstancesList().stream()
            .anyMatch(policy -> policy.getPolicy().getDisksList().stream()
                    .anyMatch(attachedDisk
                            -> attachedDisk.getDeviceName().contains(NEW_PERSISTENT_DISK_NAME))));
  }

  @Test
  public void createBatchNotificationTest()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String topicId = "newTopic";
    Job job = CreateBatchNotification
            .createBatchNotification(PROJECT_ID, REGION, NOTIFICATION_NAME, topicId);

    Assert.assertNotNull(job);
    ACTIVE_JOBS.add(job);

    Assert.assertTrue(job.getName().contains(NOTIFICATION_NAME));
    Assert.assertTrue(job.getNotificationsList().stream()
            .anyMatch(jobNotification -> jobNotification.getPubsubTopic().contains(topicId)
                    && jobNotification.getMessage().getType() == Type.JOB_STATE_CHANGED));
    Assert.assertTrue(job.getNotificationsList().stream()
            .anyMatch(jobNotification -> jobNotification.getPubsubTopic().contains(topicId)
                    && jobNotification.getMessage().getType() == Type.TASK_STATE_CHANGED
                    && jobNotification.getMessage().getNewTaskState() == State.FAILED));
  }

  @Test
  public void createBatchCustomEventTest()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String displayName1 = "script 1";
    String displayName2 = "barrier 1";
    String displayName3 = "script 2";
    Job job = CreateBatchCustomEvent
            .createBatchCustomEvent(PROJECT_ID, REGION, CUSTOM_EVENT_NAME,
                    displayName1, displayName2, displayName3);

    Assert.assertNotNull(job);
    ACTIVE_JOBS.add(job);

    Assert.assertTrue(job.getName().contains(CUSTOM_EVENT_NAME));

    Arrays.asList(displayName1, displayName2, displayName3)
            .forEach(displayName -> Assert.assertTrue(job.getTaskGroupsList().stream()
                    .flatMap(event -> event.getTaskSpec().getRunnablesList().stream())
                    .anyMatch(runnable -> runnable.getDisplayName().equals(displayName))));
  }

  @Test
  public void createScriptJobWithNfsTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Job job = CreateScriptJobWithNfs.createScriptJobWithNfs(PROJECT_ID, REGION, NFS_JOB_NAME,
        NFS_PATH, NFS_IP_ADDRESS);

    Assert.assertNotNull(job);
    ACTIVE_JOBS.add(job);

    Assert.assertTrue(job.getName().contains(NFS_JOB_NAME));

    Assert.assertTrue(job.getTaskGroupsList().stream().anyMatch(taskGroup
            -> taskGroup.getTaskSpec().getVolumesList().stream()
            .anyMatch(volume -> volume.getNfs().getRemotePath().equals(NFS_PATH))));
    Assert.assertTrue(job.getTaskGroupsList().stream().anyMatch(taskGroup
            -> taskGroup.getTaskSpec().getVolumesList().stream()
            .anyMatch(volume -> volume.getNfs().getServer().equals(NFS_IP_ADDRESS))));
  }

  @Test
  public void createBatchLabelJobTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String labelName1 = "env";
    String labelValue1 = "env_value";
    String labelName2 = "test";
    String labelValue2 = "test_value";

    Job job = CreateBatchLabelJob.createBatchLabelJob(PROJECT_ID, REGION,
        BATCH_LABEL_JOB, labelName1, labelValue1, labelName2, labelValue2);

    Assert.assertNotNull(job);
    ACTIVE_JOBS.add(job);

    Assert.assertTrue(job.getName().contains(BATCH_LABEL_JOB));
    Assert.assertTrue(job.containsLabels(labelName1));
    Assert.assertTrue(job.containsLabels(labelName2));
    Assert.assertTrue(job.getLabelsMap().containsValue(labelValue1));
    Assert.assertTrue(job.getLabelsMap().containsValue(labelValue2));
  }

  @Test
  public void createBatchCustomNetworkTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String network = "global/networks/test-network";
    String subnet = "regions/europe-west1/subnetworks/subnet";

    Job job = CreateBatchCustomNetwork
        .createBatchCustomNetwork(PROJECT_ID, REGION, CUSTOM_NETWORK_NAME,
            network, subnet);

    Assert.assertNotNull(job);
    ACTIVE_JOBS.add(job);

    Assert.assertTrue(job.getName().contains(CUSTOM_NETWORK_NAME));
    Assert.assertTrue(job.getAllocationPolicy().getNetwork().getNetworkInterfacesList().stream()
        .anyMatch(networkName -> networkName.getNetwork().equals(network)));
    Assert.assertTrue(job.getAllocationPolicy().getNetwork().getNetworkInterfacesList().stream()
        .anyMatch(subnetName -> subnetName.getSubnetwork().equals(subnet)));
    Assert.assertTrue(job.getAllocationPolicy().getNetwork().getNetworkInterfacesList().stream()
        .anyMatch(AllocationPolicy.NetworkInterface::getNoExternalIpAddress));
  }

  @Test
  public void createJobWithAllocationPolicyLabelTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String labelName1 = "env";
    String labelValue1 = "env_value";
    String labelName2 = "test";
    String labelValue2 = "test_value";

    Job job = CreateBatchAllocationPolicyLabel
        .createBatchAllocationPolicyLabel(PROJECT_ID, REGION,
        JOB_ALLOCATION_POLICY_LABEL, labelName1, labelValue1, labelName2, labelValue2);

    Assert.assertNotNull(job);
    ACTIVE_JOBS.add(job);

    Assert.assertTrue(job.getName().contains(JOB_ALLOCATION_POLICY_LABEL));
    Assert.assertTrue(job.getAllocationPolicy().containsLabels(labelName1));
    Assert.assertTrue(job.getAllocationPolicy().containsLabels(labelName2));
    Assert.assertTrue(job.getAllocationPolicy().getLabelsMap().containsValue(labelValue1));
    Assert.assertTrue(job.getAllocationPolicy().getLabelsMap().containsValue(labelValue2));
  }

  @Test
  public void createBatchRunnableLabelTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String labelName1 = "env";
    String labelValue1 = "env_value";
    String labelName2 = "test";
    String labelValue2 = "test_value";

    Job job = CreateBatchRunnableLabel.createBatchRunnableLabel(PROJECT_ID, REGION,
        BATCH_RUNNABLE_LABEL, labelName1, labelValue1, labelName2, labelValue2);

    Assert.assertNotNull(job);
    ACTIVE_JOBS.add(job);

    Assert.assertTrue(job.getName().contains(BATCH_RUNNABLE_LABEL));
    Arrays.asList(labelName1, labelName2)
        .forEach(labelName -> Assert.assertTrue(job.getTaskGroupsList().stream()
            .flatMap(event -> event.getTaskSpec().getRunnablesList().stream())
            .anyMatch(runnable -> runnable.containsLabels(labelName))));
    Arrays.asList(labelValue1, labelValue2)
        .forEach(labelValue -> Assert.assertTrue(job.getTaskGroupsList().stream()
            .flatMap(event -> event.getTaskSpec().getRunnablesList().stream())
            .anyMatch(runnable -> runnable.getLabelsMap().containsValue(labelValue))));
  }

  private void createEmptyDisk(String projectId, String zone, String diskName,
                                     String diskType, long diskSizeGb)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (DisksClient disksClient = DisksClient.create()) {
      // Set the disk properties.
      Disk disk = Disk.newBuilder()
              .setName(diskName)
              .setZone(zone)
              .setType(diskType)
              .setSizeGb(diskSizeGb)
              .build();

      // Create the Insert disk request.
      InsertDiskRequest insertDiskRequest = InsertDiskRequest.newBuilder()
              .setProject(projectId)
              .setZone(zone)
              .setDiskResource(disk)
              .build();

      // Wait for the create disk operation to complete.
      disksClient.insertAsync(insertDiskRequest).get(3, TimeUnit.MINUTES);

      TimeUnit.SECONDS.sleep(5);
    }
  }
}
