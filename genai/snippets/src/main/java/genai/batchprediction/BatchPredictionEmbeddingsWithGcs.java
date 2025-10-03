/*
 * Copyright 2025 Google LLC
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

package genai.batchprediction;

// [START googlegenaisdk_batchpredict_embeddings_with_gcs]

import static com.google.genai.types.JobState.Known.JOB_STATE_CANCELLED;
import static com.google.genai.types.JobState.Known.JOB_STATE_FAILED;
import static com.google.genai.types.JobState.Known.JOB_STATE_PAUSED;
import static com.google.genai.types.JobState.Known.JOB_STATE_SUCCEEDED;

import com.google.genai.Client;
import com.google.genai.types.BatchJob;
import com.google.genai.types.BatchJobDestination;
import com.google.genai.types.BatchJobSource;
import com.google.genai.types.CreateBatchJobConfig;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.JobState;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class BatchPredictionEmbeddingsWithGcs {

  public static void main(String[] args) throws InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "text-embedding-005";
    String outputGcsUri = "gs://your-bucket/your-prefix";
    createBatchJob(modelId, outputGcsUri);
  }

  // Creates a batch prediction job with embedding model and Google Cloud Storage
  public static JobState createBatchJob(String modelId, String outputGcsUri)
      throws InterruptedException {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("us-central1")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      // See the documentation:
      // https://googleapis.github.io/java-genai/javadoc/com/google/genai/Batches.html
      BatchJobSource batchJobSource =
          BatchJobSource.builder()
              // Source link:
              // https://storage.cloud.google.com/cloud-samples-data/generative-ai/embeddings/embeddings_input.jsonl
              .gcsUri("gs://cloud-samples-data/generative-ai/embeddings/embeddings_input.jsonl")
              .format("jsonl")
              .build();

      CreateBatchJobConfig batchJobConfig =
          CreateBatchJobConfig.builder()
              .displayName("your-display-name")
              .dest(BatchJobDestination.builder().gcsUri(outputGcsUri).format("jsonl").build())
              .build();

      BatchJob batchJob = client.batches.create(modelId, batchJobSource, batchJobConfig);

      String jobName =
          batchJob.name().orElseThrow(() -> new IllegalStateException("Failed to get job name."));
      JobState jobState =
          batchJob.state().orElseThrow(() -> new IllegalStateException("Failed to get job state."));

      System.out.println("Job name: " + jobName);
      System.out.println("Job state: " + jobState);
      // Job name:
      // projects/{PROJECT_ID}/locations/us-central1/batchPredictionJobs/6205497615459549184
      // Job state: JOB_STATE_PENDING

      // See the documentation:
      // https://googleapis.github.io/java-genai/javadoc/com/google/genai/types/BatchJob.html
      Set<JobState.Known> completedStates =
          EnumSet.of(JOB_STATE_SUCCEEDED, JOB_STATE_FAILED, JOB_STATE_CANCELLED, JOB_STATE_PAUSED);

      while (!completedStates.contains(jobState.knownEnum())) {
        TimeUnit.SECONDS.sleep(30);
        batchJob = client.batches.get(jobName, null);
        jobState =
            batchJob
                .state()
                .orElseThrow(() -> new IllegalStateException("Failed to get job state."));
        System.out.println("Job state: " + jobState);
      }
      // Example response:
      // Job state: JOB_STATE_QUEUED
      // Job state: JOB_STATE_RUNNING
      // Job state: JOB_STATE_RUNNING
      // ...
      // Job state: JOB_STATE_SUCCEEDED
      return jobState;
    }
  }
}
// [END googlegenaisdk_batchpredict_embeddings_with_gcs]
