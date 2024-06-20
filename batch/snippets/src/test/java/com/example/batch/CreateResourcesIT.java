package com.example.batch;

import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.batch.v1.Job;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
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
  private static final String SERVICE_ACCOUNT_JOB = "test-job" + UUID.randomUUID().toString().substring(0, 7);
  private static final String SECRET_MANAGER_JOB = "test-job" + UUID.randomUUID().toString().substring(0, 7);
  private static final String GPU_JOB = "test-job" + UUID.randomUUID().toString().substring(0, 7);
  private static final String LOCAL_SSD_JOB = "test-job" + UUID.randomUUID().toString().substring(0, 7);
  private static final String PERSISTENT_DISK_JOB = "test-job" + UUID.randomUUID().toString().substring(0, 7);
  private static final String LOCAL_SSD_NAME = "test-disk" + UUID.randomUUID().toString().substring(0, 7);
  private static final List<Job> ACTIVE_JOBS = new ArrayList<>();

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
            .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeClass
  public static void setUp() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
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
    safeDeleteJob(SERVICE_ACCOUNT_JOB);
    safeDeleteJob(SECRET_MANAGER_JOB);
    safeDeleteJob(GPU_JOB);
    safeDeleteJob(LOCAL_SSD_JOB);
    safeDeleteJob(PERSISTENT_DISK_JOB);
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
    Job job = CreateBatchCustomServiceAccount
            .createCustomServiceAccount(PROJECT_ID, REGION, SERVICE_ACCOUNT_JOB, null);

    Assert.assertNotNull(job);
    ACTIVE_JOBS.add(job);

    Assert.assertTrue(job.getName().contains(SERVICE_ACCOUNT_JOB));
    Assert.assertNotNull(job.getAllocationPolicy().getServiceAccount());
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
    String gpuType = "nvidia-tesla-t4";
    int count = 2;
    Job job = CreateGpuJob
            .createGpuJob(PROJECT_ID, REGION, GPU_JOB, true, gpuType, count);

    Assert.assertNotNull(job);
    ACTIVE_JOBS.add(job);

    Assert.assertTrue(job.getName().contains(GPU_JOB));
    Assert.assertTrue(job.getAllocationPolicy().getInstancesList().stream().anyMatch(instance
        -> instance.getInstallGpuDrivers() && instance.getPolicy().getAcceleratorsList().stream()
            .anyMatch(accelerator
                -> accelerator.getType().contains(gpuType) && accelerator.getCount() == count)));
  }

  @Test
  public void createLocalSsdJobTest()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String type = "c3d-standard-360-lssd";
    Job job = CreateLocalSsdJob
            .createLocalSsdJob(PROJECT_ID, REGION, LOCAL_SSD_JOB, LOCAL_SSD_NAME, 375, type);

    Assert.assertNotNull(job);
    ACTIVE_JOBS.add(job);

    Assert.assertTrue(job.getName().contains(LOCAL_SSD_JOB));
    Assert.assertNotNull(job.getAllocationPolicy().getInstancesList());
    Assert.assertTrue(job.getAllocationPolicy().getInstancesList().stream()
            .anyMatch(instance -> instance.getPolicy().getMachineType().contains(type)
                && instance.getPolicy().getDisksList().stream().anyMatch(attachedDisk
                    -> attachedDisk.getDeviceName().contains(LOCAL_SSD_NAME))));
  }

//  @Test
  public void createPersistentDiskJobTest()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String type = "c3d-standard-360-lssd";
    Job job = CreatePersistentDiskJob
            .createPersistentDiskJob(PROJECT_ID, REGION, PERSISTENT_DISK_JOB, "newDisk", 375, "", "", "");

    Assert.assertNotNull(job);
    ACTIVE_JOBS.add(job);

    Assert.assertTrue(job.getName().contains(PERSISTENT_DISK_JOB));
    Assert.assertNotNull(job.getAllocationPolicy().getInstancesList());
    Assert.assertTrue(job.getAllocationPolicy().getInstancesList().stream()
            .anyMatch(instance -> instance.getPolicy().getMachineType().contains(type)
                    && instance.getPolicy().getDisksList().stream().anyMatch(attachedDisk
                    -> attachedDisk.getDeviceName().contains(LOCAL_SSD_NAME))));
  }
}
