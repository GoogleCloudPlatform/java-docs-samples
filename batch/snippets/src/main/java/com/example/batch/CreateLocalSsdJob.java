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

// [START batch_create_local_ssd_job]

import com.google.cloud.batch.v1.AllocationPolicy;
import com.google.cloud.batch.v1.AllocationPolicy.AttachedDisk;
import com.google.cloud.batch.v1.AllocationPolicy.Disk;
import com.google.cloud.batch.v1.AllocationPolicy.InstancePolicy;
import com.google.cloud.batch.v1.AllocationPolicy.InstancePolicyOrTemplate;
import com.google.cloud.batch.v1.BatchServiceClient;
import com.google.cloud.batch.v1.CreateJobRequest;
import com.google.cloud.batch.v1.Job;
import com.google.cloud.batch.v1.LogsPolicy;
import com.google.cloud.batch.v1.Runnable;
import com.google.cloud.batch.v1.Runnable.Script;
import com.google.cloud.batch.v1.TaskGroup;
import com.google.cloud.batch.v1.TaskSpec;
import com.google.cloud.batch.v1.Volume;
import com.google.protobuf.Duration;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateLocalSsdJob {

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
    // The name of a local SSD created for this job.
    String localSsdName = "SSD-NAME";
    // The machine type, which can be predefined or custom, of the job's VMs.
    // The allowed number of local SSDs depends on the machine type
    // for your job's VMs are listed on: https://cloud.google.com/compute/docs/disks#localssds
    String machineType = "c3d-standard-8-lssd";
    // The size of all the local SSDs in GB. Each local SSD is 375 GB,
    // so this value must be a multiple of 375 GB.
    // For example, for 2 local SSDs, set this value to 750 GB.
    int ssdSize = 375;

    createLocalSsdJob(projectId, region, jobName, localSsdName, ssdSize, machineType);
  }

  // Create a job that uses local SSDs
  public static Job createLocalSsdJob(String projectId, String region, String jobName,
                                      String localSsdName, int ssdSize, String machineType)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (BatchServiceClient batchServiceClient = BatchServiceClient.create()) {
      // Define what will be done as part of the job.
      Runnable runnable =
          Runnable.newBuilder()
              .setScript(
                  Script.newBuilder()
                      .setText(
                          "echo Hello world! This is task ${BATCH_TASK_INDEX}. "
                                  + "This job has a total of ${BATCH_TASK_COUNT} tasks.")
                      // You can also run a script from a file. Just remember, that needs to be a
                      // script that's already on the VM that will be running the job.
                      // Using setText() and setPath() is mutually exclusive.
                      // .setPath("/tmp/test.sh")
                      .build())
              .build();

      Volume volume = Volume.newBuilder()
          .setDeviceName(localSsdName)
          .setMountPath("/mnt/disks/" + localSsdName)
          .addMountOptions("rw")
          .addMountOptions("async")
          .build();

      TaskSpec task = TaskSpec.newBuilder()
          // Jobs can be divided into tasks. In this case, we have only one task.
          .addVolumes(volume)
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

      // Policies are used to define on what kind of virtual machines the tasks will run on.
      InstancePolicy policy = InstancePolicy.newBuilder()
          .setMachineType(machineType)
          .addDisks(AttachedDisk.newBuilder()
              .setDeviceName(localSsdName)
              // For example, local SSD uses type "local-ssd".
              // Persistent disks and boot disks use "pd-balanced", "pd-extreme", "pd-ssd"
              // or "pd-standard".
              .setNewDisk(Disk.newBuilder().setSizeGb(ssdSize).setType("local-ssd")))
          .build();

      AllocationPolicy allocationPolicy =
          AllocationPolicy.newBuilder()
              .addInstances(
                  InstancePolicyOrTemplate.newBuilder()
                      .setPolicy(policy)
                      .build())
              .build();

      Job job =
          Job.newBuilder()
              .addTaskGroups(taskGroup)
              .setAllocationPolicy(allocationPolicy)
              .putLabels("env", "testing")
              .putLabels("type", "script")
              // We use Cloud Logging as it's an out of the box available option.
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
}
// [END batch_create_local_ssd_job]
