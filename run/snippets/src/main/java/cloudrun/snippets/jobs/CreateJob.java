/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package cloudrun.snippets.jobs;

// [START cloudrun_create_job]
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import com.google.cloud.run.v2.Container;
import com.google.cloud.run.v2.ExecutionTemplate;
import com.google.cloud.run.v2.Job;
import com.google.cloud.run.v2.JobsClient;
import com.google.cloud.run.v2.LocationName;
import com.google.cloud.run.v2.TaskTemplate;

public class CreateJob {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        // TODO(developer): Replace these variables before running the sample.
        String projectId = "your-project-id";
        String location = "us-central1";
        String jobId = "my-job-id";
        String imageUrl = "us-docker.pkg.dev/cloudrun/container/hello";
        createJob(projectId, location, jobId, imageUrl);
    }

    public static void createJob(String projectId, String location, String jobId, String imageUrl) throws IOException, InterruptedException, ExecutionException {
        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests.
        try (JobsClient jobsClient = JobsClient.create()) {
            LocationName parent = LocationName.of(projectId, location);
            // Define job
            // Shows minimum necessary configuration
            Job job = Job.newBuilder()
                    .setTemplate(ExecutionTemplate.newBuilder()
                            .setTemplate(TaskTemplate.newBuilder()
                                    .addContainers(Container.newBuilder().setImage(imageUrl))))
                    .build();
            // Send request
            Job response = jobsClient.createJobAsync(parent, job, jobId).get();
            // Example usage of the Job object
            System.out.println("Created job: " + response.getName());
            System.out.println("With spec:\n" + response.getTemplate());
        }
    }
}
// [END cloudrun_create_job]