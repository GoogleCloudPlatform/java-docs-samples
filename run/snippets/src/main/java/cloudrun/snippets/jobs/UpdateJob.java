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

// [START cloudrun_update_job]
import com.google.cloud.run.v2.Container;
import com.google.cloud.run.v2.EnvVar;
import com.google.cloud.run.v2.ExecutionTemplate;
import com.google.cloud.run.v2.RevisionTemplate;
import com.google.cloud.run.v2.TaskTemplate;
import com.google.cloud.run.v2.Job;
import com.google.cloud.run.v2.JobName;
import com.google.cloud.run.v2.JobsClient;
import com.google.cloud.run.v2.UpdateJobRequest;
import com.google.protobuf.Timestamp;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class UpdateJob {

    public static void main(String[] args)
            throws IOException, InterruptedException, ExecutionException {
        // TODO(developer): Replace these variables before running the sample.
        String projectId = "your-project-id";
        String location = "us-central1";
        String jobId = "my-job-id";
        updateJob(projectId, location, jobId);
    }

    public static void updateJob(String projectId, String location, String jobId)
            throws IOException, InterruptedException, ExecutionException {
        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests.
        try (JobsClient jobsClient = JobsClient.create()) {
            // Get previous job
            JobName name = JobName.of(projectId, location, jobId);
            Job job = jobsClient.getJob(name);
            // Define new environment variables for the job
            List<EnvVar> envVars = new ArrayList<EnvVar>();
            envVars.add(EnvVar.newBuilder().setName("FOO").setValue("BAR").build());
            //.addAllEnv(envVars)
            // .mergeFrom(job.getTemplate().getTemplate().getContainers(0))
            //   .setTemplate(
            //     ExecutionTemplate.newBuilder().setTemplate(
            //         TaskTemplate.newBuilder().addContainers(
            //             Container.newBuilder()
            //     ))
            Job newJob = Job.newBuilder().mergeFrom(job)
                    .setTemplate(ExecutionTemplate.newBuilder()
                            .setTemplate(TaskTemplate.newBuilder()
                                    .addContainers(Container.newBuilder().mergeFrom(job.getTemplate().getTemplate().getContainers(0)).addAllEnv(envVars))))
                    .build();

            Job response = jobsClient.updateJobAsync(newJob).get();
            System.out.println("Updated job: " + response.getName());
            Timestamp ts = job.getUpdateTime();
            Instant instant = Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
            System.out.println("Updated at: " + instant.toString());
        }
    }
}
// [END cloudrun_update_job]
