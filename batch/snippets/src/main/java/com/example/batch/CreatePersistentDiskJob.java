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

// [START batch_create_persistent_disk_job]

import com.google.cloud.batch.v1.AllocationPolicy;
import com.google.cloud.batch.v1.AllocationPolicy.AttachedDisk;
import com.google.cloud.batch.v1.AllocationPolicy.Disk;
import com.google.cloud.batch.v1.AllocationPolicy.InstancePolicy;
import com.google.cloud.batch.v1.AllocationPolicy.InstancePolicyOrTemplate;
import com.google.cloud.batch.v1.AllocationPolicy.LocationPolicy;
import com.google.cloud.batch.v1.BatchServiceClient;
import com.google.cloud.batch.v1.CreateJobRequest;
import com.google.cloud.batch.v1.Job;
import com.google.cloud.batch.v1.LogsPolicy;
import com.google.cloud.batch.v1.Runnable;
import com.google.cloud.batch.v1.Runnable.Script;
import com.google.cloud.batch.v1.TaskGroup;
import com.google.cloud.batch.v1.TaskSpec;
import com.google.cloud.batch.v1.Volume;
import com.google.common.collect.Lists;
import com.google.protobuf.Duration;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreatePersistentDiskJob {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the region you want to use to run the job. Regions that are
    // available for Batch are listed on: https://cloud.google.com/batch/docs/get-started#locations
    String region = "europe-central2";
    // The name of the job that will be created.
    // It needs to be unique for each project and region pair.
    String jobName = "JOB_NAME";
    // The size of the new persistent disk in GB.
    // The allowed sizes depend on the type of persistent disk,
    // but the minimum is often 10 GB (10) and the maximum is often 64 TB (64000).
    int diskSize = 10;
    // The name of the new persistent disk.
    String newPersistentDiskName = "DISK-NAME";
    // The name of an existing persistent disk.
    String existingPersistentDiskName = "EXISTING-DISK-NAME";
    // The location of an existing persistent disk. For more info :
    // https://cloud.google.com/batch/docs/create-run-job-storage#gcloud
    String location = "regions/us-central1";
    // The disk type of the new persistent disk, either pd-standard,
    // pd-balanced, pd-ssd, or pd-extreme. For Batch jobs, the default is pd-balanced.
    String newDiskType = "pd-balanced";

    createPersistentDiskJob(projectId, region, jobName, newPersistentDiskName,
            diskSize, existingPersistentDiskName, location, newDiskType);
  }

  // Creates a job that attaches and mounts an existing persistent disk and a new persistent disk
  public static Job createPersistentDiskJob(String projectId, String region, String jobName,
                                            String newPersistentDiskName, int diskSize,
                                            String existingPersistentDiskName,
                                            String location, String newDiskType)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (BatchServiceClient batchServiceClient = BatchServiceClient.create()) {
      // Define what will be done as part of the job.
      String text = "echo Hello world from task ${BATCH_TASK_INDEX}. "
              + ">> /mnt/disks/NEW_PERSISTENT_DISK_NAME/output_task_${BATCH_TASK_INDEX}.txt";
      Runnable runnable =
          Runnable.newBuilder()
              .setScript(
                  Script.newBuilder()
                      .setText(text)
                      // You can also run a script from a file. Just remember, that needs to be a
                      // script that's already on the VM that will be running the job.
                      // Using setText() and setPath() is mutually exclusive.
                      // .setPath("/tmp/test.sh")
                      .build())
              .build();

      TaskSpec task = TaskSpec.newBuilder()
              // Jobs can be divided into tasks. In this case, we have only one task.
              .addAllVolumes(volumes(newPersistentDiskName, existingPersistentDiskName))
              .addRunnables(runnable)
              .setMaxRetryCount(2)
              .setMaxRunDuration(Duration.newBuilder().setSeconds(3600).build())
              .build();

      // Tasks are grouped inside a job using TaskGroups.
      // Currently, it's possible to have only one task group.
      TaskGroup taskGroup = TaskGroup.newBuilder()
          .setTaskCount(3)
          .setParallelism(1)
          .setTaskSpec(task)
          .build();

      // Policies are used to define the type of virtual machines the tasks will run on.
      InstancePolicy policy = InstancePolicy.newBuilder()
              .addAllDisks(attachedDisks(newPersistentDiskName, diskSize, newDiskType,
                  projectId, location, existingPersistentDiskName))
              .build();

      AllocationPolicy allocationPolicy =
          AllocationPolicy.newBuilder()
              .addInstances(
                  InstancePolicyOrTemplate.newBuilder()
                      .setPolicy(policy))
                  .setLocation(LocationPolicy.newBuilder().addAllowedLocations(location))
              .build();

      Job job =
          Job.newBuilder()
              .addTaskGroups(taskGroup)
              .setAllocationPolicy(allocationPolicy)
              .putLabels("env", "testing")
              .putLabels("type", "script")
              // We use Cloud Logging as it's an out-of-the-box option.
              .setLogsPolicy(
                  LogsPolicy.newBuilder().setDestination(LogsPolicy.Destination.CLOUD_LOGGING))
              .build();

      CreateJobRequest createJobRequest =
          CreateJobRequest.newBuilder()
              // The job's parent is the region in which the job will run.
              .setParent(String.format("projects/%s/locations/%s", projectId, region))
              .setJob(job)
              .setJobId(jobName)
              .build();

      Job result =
          batchServiceClient
              .createJobCallable()
              .futureCall(createJobRequest)
              .get(5, TimeUnit.MINUTES);

      System.out.printf("Successfully created the job: %s", result.getName());

      return result;
    }
  }

  // Creates link to existing disk and creates configuration for new disk
  private static Iterable<AttachedDisk> attachedDisks(String newPersistentDiskName, int diskSize,
                                                      String newDiskType, String projectId,
                                                      String existingPersistentDiskLocation,
                                                      String existingPersistentDiskName) {
    AttachedDisk newDisk = AttachedDisk.newBuilder()
            .setDeviceName(newPersistentDiskName)
            .setNewDisk(Disk.newBuilder().setSizeGb(diskSize).setType(newDiskType))
            .build();

    String diskPath = String.format("projects/%s/%s/disks/%s", projectId,
            existingPersistentDiskLocation, existingPersistentDiskName);

    AttachedDisk existingDisk = AttachedDisk.newBuilder()
            .setDeviceName(existingPersistentDiskName)
            .setExistingDisk(diskPath)
            .build();

    return Lists.newArrayList(existingDisk, newDisk);
  }

  // Describes a volume and parameters for it to be mounted to a VM.
  private static Iterable<Volume> volumes(String newPersistentDiskName,
                                          String existingPersistentDiskName) {
    Volume newVolume = Volume.newBuilder()
            .setDeviceName(newPersistentDiskName)
            .setMountPath("/mnt/disks/" + newPersistentDiskName)
            .addMountOptions("rw")
            .addMountOptions("async")
            .build();

    Volume existingVolume = Volume.newBuilder()
            .setDeviceName(existingPersistentDiskName)
            .setMountPath("/mnt/disks/" + existingPersistentDiskName)
            .build();

    return Lists.newArrayList(newVolume, existingVolume);
  }
}
// [END batch_create_persistent_disk_job]
