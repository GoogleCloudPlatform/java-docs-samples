/*
 * Copyright 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// [START dataproc_submit_job]
import com.google.cloud.dataproc.v1.Job;
import com.google.cloud.dataproc.v1.JobControllerClient;
import com.google.cloud.dataproc.v1.JobControllerSettings;
import com.google.cloud.dataproc.v1.JobPlacement;
import com.google.cloud.dataproc.v1.PySparkJob;
import java.io.IOException;

public class SubmitJob {

  public static void submitJob(
      String projectId, String region, String clusterName, String job_file_path)
      throws IOException {
    String myEndpoint = String.format("%s-dataproc.googleapis.com:443", region);

    // Configure the settings for the cluster controller client
    JobControllerSettings jobControllerSettings =
        JobControllerSettings.newBuilder().setEndpoint(myEndpoint).build();

    // Create a cluster controller client with the configured settings. We only need to create
    // the client once, and can be reused for multiple requests. Using a try-with-resources
    // will close the client for us, but this can also be done manually with the .close() method.
    try (JobControllerClient jobControllerClient =
        JobControllerClient.create(jobControllerSettings)) {

      // Configure the settings for our job
      JobPlacement jobPlacement = JobPlacement.newBuilder().setClusterName(clusterName).build();
      PySparkJob pySparkJob = PySparkJob.newBuilder().setMainPythonFileUri(job_file_path).build();
      Job job = Job.newBuilder().setPlacement(jobPlacement).setPysparkJob(pySparkJob).build();

      Job jobOperation = jobControllerClient.submitJob(projectId, region, job);

      String jobId = jobOperation.getReference().getJobId();

      while (true) {
        Job jobInfo = jobControllerClient.getJob(projectId, region, jobId);
        if (jobInfo.getStatus().getState().toString() == "ERROR") {
          System.out.println(
              String.format("Job %s failed: %s", jobId, jobInfo.getStatus().getDetails()));
          break;
        } else if (jobInfo.getStatus().getState().toString() == "DONE") {
          System.out.println(String.format("Job %s finished.", jobId));
          break;
        }
      }
    } catch (IOException e) {
      // Likely this would occur due to issues authenticating with GCP. Make sure the environment
      // variable GOOGLE_APPLICATION_CREDENTIALS is configured.
      System.out.println("Error creating the cluster controller client: \n" + e.toString());
    }
  }
}
// [END dataproc_submit_job]
