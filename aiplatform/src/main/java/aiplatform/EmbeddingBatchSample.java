/*
 * Copyright 2024 Google LLC
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

package aiplatform;

// [START generativeaionvertexai_embedding_batch]

import com.google.cloud.aiplatform.v1.BatchPredictionJob;
import com.google.cloud.aiplatform.v1.GcsDestination;
import com.google.cloud.aiplatform.v1.GcsSource;
import com.google.cloud.aiplatform.v1.JobServiceClient;
import com.google.cloud.aiplatform.v1.JobServiceSettings;
import com.google.cloud.aiplatform.v1.LocationName;
import java.io.IOException;

public class EmbeddingBatchSample {

  public static void main(String[] args) throws IOException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    String project = "YOUR_PROJECT_ID";
    String location = "us-central1";
    // inputUri: URI of the input dataset.
    // Could be a BigQuery table or a Google Cloud Storage file.
    // E.g. "gs://[BUCKET]/[DATASET].jsonl" OR "bq://[PROJECT].[DATASET].[TABLE]"
    String inputUri = "gs://cloud-samples-data/generative-ai/embeddings/embeddings_input.jsonl";
    // outputUri: URI where the output will be stored.
    // Could be a BigQuery table or a Google Cloud Storage file.
    // E.g. "gs://[BUCKET]/[OUTPUT].jsonl" OR "bq://[PROJECT].[DATASET].[TABLE]"
    String outputUri = "gs://YOUR_BUCKET/embedding_batch_output";
    String textEmbeddingModel = "text-embedding-005";

    embeddingBatchSample(project, location, inputUri, outputUri, textEmbeddingModel);
  }

  // Generates embeddings from text using batch processing.
  // Read more: https://cloud.google.com/vertex-ai/generative-ai/docs/embeddings/batch-prediction-genai-embeddings
  public static BatchPredictionJob embeddingBatchSample(
      String project, String location, String inputUri, String outputUri, String textEmbeddingModel)
      throws IOException {
    BatchPredictionJob response;
    JobServiceSettings jobServiceSettings =  JobServiceSettings.newBuilder()
        .setEndpoint("us-central1-aiplatform.googleapis.com:443").build();
    LocationName parent = LocationName.of(project, location);
    String modelName = String.format("projects/%s/locations/%s/publishers/google/models/%s",
        project, location, textEmbeddingModel);

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (JobServiceClient client = JobServiceClient.create(jobServiceSettings)) {
      BatchPredictionJob batchPredictionJob =
          BatchPredictionJob.newBuilder()
              .setDisplayName("my embedding batch job " + System.currentTimeMillis())
              .setModel(modelName)
              .setInputConfig(
                  BatchPredictionJob.InputConfig.newBuilder()
                      .setGcsSource(GcsSource.newBuilder().addUris(inputUri).build())
                      .setInstancesFormat("jsonl")
                      .build())
              .setOutputConfig(
                  BatchPredictionJob.OutputConfig.newBuilder()
                      .setGcsDestination(GcsDestination.newBuilder()
                          .setOutputUriPrefix(outputUri).build())
                      .setPredictionsFormat("jsonl")
                      .build())
              .build();

      response = client.createBatchPredictionJob(parent, batchPredictionJob);

      System.out.format("response: %s\n", response);
      System.out.format("\tName: %s\n", response.getName());
    }
    return response;
  }
}
// [END generativeaionvertexai_embedding_batch]
