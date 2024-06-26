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

// [START batch_custom_events]

import com.google.cloud.batch.v1.BatchServiceClient;
import com.google.cloud.batch.v1.CreateJobRequest;
import com.google.cloud.batch.v1.Job;
import com.google.cloud.batch.v1.LogsPolicy;
import com.google.cloud.batch.v1.LogsPolicy.Destination;
import com.google.cloud.batch.v1.Runnable;
import com.google.cloud.batch.v1.Runnable.Barrier;
import com.google.cloud.batch.v1.Runnable.Script;
import com.google.cloud.batch.v1.TaskGroup;
import com.google.cloud.batch.v1.TaskSpec;
import com.google.protobuf.Duration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateBatchCustomEvent {

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
    // Name of the runnable, which must be unique
    // within the job. For example: script 1, barrier 1, and script 2.
    String displayName1 = "script 1";
    String displayName2 = "barrier 1";
    String displayName3 = "script 2";

    createBatchCustomEvent(projectId, region, jobName, displayName1, displayName2, displayName3);
  }

  // Configure custom status events, which describe a job's runnables,
  // when you create and run a Batch job.
  public static Job createBatchCustomEvent(String projectId, String region, String jobName,
                                           String displayName1, String displayName2,
                                           String displayName3)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (BatchServiceClient batchServiceClient = BatchServiceClient.create()) {
      TaskSpec task = TaskSpec.newBuilder()
              // Jobs can be divided into tasks. In this case, we have only one task.
              .addAllRunnables(buildRunnables(displayName1, displayName2, displayName3))
              .setMaxRetryCount(2)
              .setMaxRunDuration(Duration.newBuilder().setSeconds(3600).build())
              .build();

      // Tasks are grouped inside a job using TaskGroups.
      // Currently, it's possible to have only one task group.
      TaskGroup taskGroup = TaskGroup.newBuilder()
          .setTaskCount(3)
          .setParallelism(3)
          .setTaskSpec(task)
          .build();

      Job job =
          Job.newBuilder()
              .addTaskGroups(taskGroup)
              .putLabels("env", "testing")
              .putLabels("type", "script")
              // We use Cloud Logging as it's an out of the box available option.
              .setLogsPolicy(
                  LogsPolicy.newBuilder().setDestination(Destination.CLOUD_LOGGING))
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

  // Create runnables with custom scripts
  private static Iterable<Runnable> buildRunnables(String displayName1, String displayName2,
                                                   String displayName3) {
    List<Runnable> runnables = new ArrayList<>();

    // Define what will be done as part of the job.
    runnables.add(Runnable.newBuilder()
        .setDisplayName(displayName1)
        .setScript(
            Script.newBuilder()
                .setText(
                    "echo Hello world from script 1 for task ${BATCH_TASK_INDEX}")
                // You can also run a script from a file. Just remember, that needs to be a
                // script that's already on the VM that will be running the job.
                // Using setText() and setPath() is mutually exclusive.
                // .setPath("/tmp/test.sh")
                )
        .build());

    runnables.add(Runnable.newBuilder()
            .setDisplayName(displayName2)
            .setBarrier(Barrier.newBuilder())
            .build());

    runnables.add(Runnable.newBuilder()
        .setDisplayName(displayName3)
        .setScript(
            Script.newBuilder()
                .setText("echo Hello world from script 2 for task ${BATCH_TASK_INDEX}"))
        .build());

    runnables.add(Runnable.newBuilder()
        .setScript(
            Script.newBuilder()
                // Replace DESCRIPTION with a description
                // for the custom status event—for example, halfway done.
                .setText("sleep 30; echo '{\"batch/custom/event\": \"DESCRIPTION\"}'; sleep 30"))
        .build());

    return runnables;
  }
}
// [END batch_custom_events]
