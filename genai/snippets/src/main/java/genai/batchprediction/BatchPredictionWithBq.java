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

// [START googlegenaisdk_batchpredict_with_bq]

import static com.google.genai.types.JobState.Known.JOB_STATE_CANCELLED;
import static com.google.genai.types.JobState.Known.JOB_STATE_FAILED;
import static com.google.genai.types.JobState.Known.JOB_STATE_PAUSED;
import static com.google.genai.types.JobState.Known.JOB_STATE_SUCCEEDED;

import com.google.genai.Client;
import com.google.genai.types.BatchJob;
import com.google.genai.types.BatchJobDestination;
import com.google.genai.types.BatchJobSource;
import com.google.genai.types.CreateBatchJobConfig;
import com.google.genai.types.GetBatchJobConfig;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.JobState;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class BatchPredictionWithBq {

  public static void main(String[] args) throws InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // To use a tuned model, set the model param to your tuned model using the following format:
    // modelId = "projects/{PROJECT_ID}/locations/{LOCATION}/models/{MODEL_ID}
    String modelId = "gemini-2.5-flash";
    String outputUri = "bq://your-project.your_dataset.your_table";
    createBatchJob(modelId, outputUri);
  }

  // Creates a batch prediction job with Google BigQuery.
  public static JobState createBatchJob(String modelId, String outputUri)
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
              .bigqueryUri("bq://storage-samples.generative_ai.batch_requests_for_multimodal_input")
              .format("bigquery")
              .build();

      CreateBatchJobConfig batchJobConfig =
          CreateBatchJobConfig.builder()
              .displayName("your-display-name")
              .dest(BatchJobDestination.builder().bigqueryUri(outputUri).format("bigquery").build())
              .build();

      BatchJob batchJob = client.batches.create(modelId, batchJobSource, batchJobConfig);

      String jobName =
          batchJob.name().orElseThrow(() -> new IllegalStateException("Missing job name"));
      JobState jobState =
          batchJob.state().orElseThrow(() -> new IllegalStateException("Missing job state"));
      System.out.println("Job name: " + jobName);
      System.out.println("Job state: " + jobState);
      // Job name:
      // projects/.../locations/.../batchPredictionJobs/3189981423167602688
      // Job state: JOB_STATE_PENDING

      // See the documentation:
      // https://googleapis.github.io/java-genai/javadoc/com/google/genai/types/BatchJob.html
      Set<JobState.Known> completedStates =
          EnumSet.of(JOB_STATE_SUCCEEDED, JOB_STATE_FAILED, JOB_STATE_CANCELLED, JOB_STATE_PAUSED);

      while (!completedStates.contains(jobState.knownEnum())) {
        TimeUnit.SECONDS.sleep(30);
        batchJob = client.batches.get(jobName, GetBatchJobConfig.builder().build());
        jobState =
            batchJob
                .state()
                .orElseThrow(() -> new IllegalStateException("Missing job state during polling"));
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
// [END googlegenaisdk_batchpredict_with_bq]
