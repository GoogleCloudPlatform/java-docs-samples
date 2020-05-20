/*
 * Copyright 2020 Google LLC
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

// [START dataproc_submit_hadoop_fs_job]
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.dataproc.v1.*;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubmitHadoopFSJob {

    public static ArrayList<String> stringToList(String s) {
        return new ArrayList<>(Arrays.asList(s.split(" ")));
    }

    public static void submitHadoopFSQuery() throws IOException, InterruptedException {
        // TODO(developer): Replace these variables before running the sample.
        String projectId = "your-project-id";
        String region = "your-project-region";
        String clusterName = "your-cluster-name";
        String hadoopFSQuery = "your-hadoop-fs-query";
        submitHadoopFSJob(projectId, region, clusterName, hadoopFSQuery);
    }

    public static void submitHadoopFSJob(
            String projectId, String region, String clusterName, String hadoopFSQuery)
            throws IOException, InterruptedException {
        String myEndpoint = String.format("%s-dataproc.googleapis.com:443", region);

        // Configure the settings for the job controller client.
        JobControllerSettings jobControllerSettings =
                JobControllerSettings.newBuilder().setEndpoint(myEndpoint).build();

        // Create a job controller client with the configured settings. Using a try-with-resources closes the client,
        // but this can also be done manually with the .close() method.
        try (JobControllerClient jobControllerClient =
                     JobControllerClient.create(jobControllerSettings)) {

            // Configure cluster placement for the job.
            JobPlacement jobPlacement = JobPlacement.newBuilder().setClusterName(clusterName).build();

            // Configure Hadoop job settings. The HadoopFS query is set here.
            HadoopJob hadoopJob = HadoopJob.newBuilder()
                    .setMainClass("org.apache.hadoop.fs.FsShell")
                    .addAllArgs(stringToList(hadoopFSQuery))
                    .build();

            Job job = Job.newBuilder().setPlacement(jobPlacement).setHadoopJob(hadoopJob).build();

            // Submit an asynchronous request to execute the job.
            OperationFuture<Job, JobMetadata> submitJobAsOperationAsyncRequest =
                    jobControllerClient.submitJobAsOperationAsync(projectId, region, job);

            Job response = submitJobAsOperationAsyncRequest.get();

            // Print output from Google Cloud Storage
            Matcher matches = Pattern.compile("gs://(.*?)/(.*)").matcher(response.getDriverOutputResourceUri());
            matches.matches();

            Storage storage = StorageOptions.getDefaultInstance().getService();
            Blob blob = storage.get(matches.group(1), String.format("%s.000000000", matches.group(2)));

            System.out.println(String.format("Job \"%s\" finished: %s",
                    response.getReference().getJobId(),
                    new String(blob.getContent())));

        } catch (ExecutionException e) {
            System.err.println(String.format("submitHadoopFSJob: %s ", e.getMessage()));
        }
    }
}
// [END dataproc_submit_hadoop_fs_job]