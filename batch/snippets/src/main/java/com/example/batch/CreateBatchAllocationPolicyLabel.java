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

// [START batch_labels_allocation]

import com.google.cloud.batch.v1.AllocationPolicy;
import com.google.cloud.batch.v1.BatchServiceClient;
import com.google.cloud.batch.v1.ComputeResource;
import com.google.cloud.batch.v1.CreateJobRequest;
import com.google.cloud.batch.v1.Job;
import com.google.cloud.batch.v1.LogsPolicy;
import com.google.cloud.batch.v1.Runnable;
import com.google.cloud.batch.v1.TaskGroup;
import com.google.cloud.batch.v1.TaskSpec;
import com.google.protobuf.Duration;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateBatchAllocationPolicyLabel {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the region you want to use to run the job. Regions that are
    // available for Batch are listed on: https://cloud.google.com/batch/docs/get-started#locations
    String region = "us-central1";
    // The name of the job that will be created.
    // It needs to be unique for each project and region pair.
    String jobName = "example-job";
    // Name of the label1 to be applied for your Job.
    String labelName1 = "VM_LABEL_NAME1";
    // Value for the label1 to be applied for your Job.
    String labelValue1 = "VM_LABEL_VALUE1";
    // Name of the label2 to be applied for your Job.
    String labelName2 = "VM_LABEL_NAME2";
    // Value for the label2 to be applied for your Job.
    String labelValue2 = "VM_LABEL_VALUE2";

    createBatchAllocationPolicyLabel(projectId, region, jobName, labelName1,
        labelValue1, labelName2, labelValue2);
  }

  // This method shows how to create a job with labels defined 
  // in the labels field of a job's allocation policy. These are 
  // applied to the job, as well as to each GPU (if any), persistent disk 
  // (all boot disks and any new storage volumes), and VM created for the job.
  public static Job createBatchAllocationPolicyLabel(String projectId, String region,
                               String jobName, String labelName1,
                               String labelValue1, String labelName2, String labelValue2)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (BatchServiceClient batchServiceClient = BatchServiceClient.create()) {

      // Define what will be done as part of the job.
      Runnable runnable =
          Runnable.newBuilder()
              .setContainer(
                  Runnable.Container.newBuilder()
                      .setImageUri("gcr.io/google-containers/busybox")
                      .setEntrypoint("/bin/sh")
                      .addCommands("-c")
                      .addCommands(
                          "echo Hello world! This is task ${BATCH_TASK_INDEX}. "
                              + "This job has a total of ${BATCH_TASK_COUNT} tasks.")
                      .build())
              .build();

      // We can specify what resources are requested by each task.
      ComputeResource computeResource =
          ComputeResource.newBuilder()
              // In milliseconds per cpu-second. This means the task requires 50% of a single CPUs.
              .setCpuMilli(2000)
              // In MiB.
              .setMemoryMib(2000)
              .build();

      TaskSpec task =
          TaskSpec.newBuilder()
              // Jobs can be divided into tasks. In this case, we have only one task.
              .addRunnables(runnable)
              .setComputeResource(computeResource)
              .setMaxRetryCount(2)
              .setMaxRunDuration(Duration.newBuilder().setSeconds(3600).build())
              .build();

      // Tasks are grouped inside a job using TaskGroups.
      // Currently, it's possible to have only one task group.
      TaskGroup taskGroup = TaskGroup.newBuilder().setTaskCount(1).setTaskSpec(task).build();

      // Policies are used to define on what kind of virtual machines the tasks will run on.
      // In this case, we tell the system to use "e2-standard-4" machine type.
      // Read more about machine types here: https://cloud.google.com/compute/docs/machine-types
      AllocationPolicy.InstancePolicy instancePolicy =
          AllocationPolicy.InstancePolicy.newBuilder().setMachineType("e2-standard-4").build();

      AllocationPolicy allocationPolicy =
          AllocationPolicy.newBuilder()
              .addInstances(AllocationPolicy.InstancePolicyOrTemplate.newBuilder()
                  .setPolicy(instancePolicy)
                  .build())
              // Labels and their value to be applied to the job and its resources
              .putLabels(labelName1, labelValue1)
              .putLabels(labelName2, labelValue2)
              .build();

      Job job =
          Job.newBuilder()
              .addTaskGroups(taskGroup)
              .setAllocationPolicy(allocationPolicy)
              // We use Cloud Logging as it's an out of the box available option.
              .setLogsPolicy(LogsPolicy.newBuilder()
                      .setDestination(LogsPolicy.Destination.CLOUD_LOGGING).build())
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
// [END batch_labels_allocation]
