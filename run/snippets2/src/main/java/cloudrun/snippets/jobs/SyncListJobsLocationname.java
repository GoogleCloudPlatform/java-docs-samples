/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloudrun.snippets.jobs;

// [START cloudrun_Jobs_ListJobs_Locationname_sync]
import java.io.IOException;
import com.google.cloud.run.v2.Job;
import com.google.cloud.run.v2.JobsClient;
import com.google.cloud.run.v2.LocationName;

public class SyncListJobsLocationname {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "PROJECT";
    String location = "us-central1";
    syncListJobsLocationname(projectId, location);
  }

  public static void syncListJobsLocationname(String projectId, String location) throws IOException {

    try (JobsClient jobsClient = JobsClient.create()) {
      LocationName parent = LocationName.of(projectId, location);
      for (Job element : jobsClient.listJobs(parent).iterateAll()) {
        // doThingsWith(element);
      }
    }
  }
}
// [END cloudrun_Jobs_ListJobs_Locationname_sync]
