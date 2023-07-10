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

// [START batch_list_tasks]
import com.google.cloud.batch.v1.BatchServiceClient;
import com.google.cloud.batch.v1.Task;
import java.io.IOException;

public class ListTasks {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the region hosts the job.
    String region = "europe-central2";
    // Name of the job which tasks you want to list.
    String jobName = "JOB_NAME";
    // Name of the group of tasks. Usually it's `group0`.
    String groupName = "group0";

    listTasks(projectId, region, jobName, groupName);
  }

  // Get a list of all jobs defined in given region.
  public static void listTasks(String projectId, String region, String jobName, String groupName)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `batchServiceClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (BatchServiceClient batchServiceClient = BatchServiceClient.create()) {

      String parent = String.format("projects/%s/locations/%s/jobs/%s/taskGroups/%s", projectId,
          region, jobName, groupName);
      for (Task task : batchServiceClient.listTasks(parent).iterateAll()) {
        System.out.println(task.getName());
      }
    }
  }
}
// [END batch_list_tasks]
