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

// [START batch_get_task]
import com.google.cloud.batch.v1.BatchServiceClient;
import com.google.cloud.batch.v1.Task;
import com.google.cloud.batch.v1.TaskName;
import java.io.IOException;

public class GetTask {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the region hosts the job.
    String region = "europe-central2";
    // The name of the job you want to retrieve information about.
    String jobName = "JOB_NAME";
    // The name of the group that owns the task you want to check. Usually it's `group0`.
    String groupName = "group0";
    // Number of the task you want to look up.
    int taskNumber = 0;

    getTask(projectId, region, jobName, groupName, taskNumber);
  }

  // Retrieve information about a Task.
  public static void getTask(String projectId, String region, String jobName, String groupName,
      int taskNumber) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `batchServiceClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (BatchServiceClient batchServiceClient = BatchServiceClient.create()) {

      Task task = batchServiceClient.getTask(TaskName.newBuilder()
          .setProject(projectId)
          .setLocation(region)
          .setJob(jobName)
          .setTaskGroup(groupName)
          .setTask(String.valueOf(taskNumber))
          .build());
      System.out.printf("Retrieved task information: %s", task.getName());
    }
  }
}
// [END batch_get_task]
