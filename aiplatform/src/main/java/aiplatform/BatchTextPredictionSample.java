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

// [START generativeaionvertexai_batch_text_predict]

import com.google.cloud.aiplatform.v1.BatchPredictionJob;
import com.google.cloud.aiplatform.v1.GcsDestination;
import com.google.cloud.aiplatform.v1.GcsSource;
import com.google.cloud.aiplatform.v1.JobServiceClient;
import com.google.cloud.aiplatform.v1.JobServiceSettings;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class BatchTextPredictionSample {

  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    String project = "YOUR_PROJECT_ID";
    String location = "us-central1";
    // inputUri: URI of the input dataset.
    // Could be a BigQuery table or a Google Cloud Storage file.
    // E.g. "gs://[BUCKET]/[DATASET].jsonl" OR "bq://[PROJECT].[DATASET].[TABLE]"
    String inputUri = "gs://cloud-samples-data/batch/prompt_for_batch_text_predict.jsonl";
    // outputUri: URI where the output will be stored.
    // Could be a BigQuery table or a Google Cloud Storage file.
    // E.g. "gs://[BUCKET]/[OUTPUT].jsonl" OR "bq://[PROJECT].[DATASET].[TABLE]"
    String outputUri = "gs://YOUR_BUCKET/batch_text_predict_output";
    String textModel = "text-bison";

    batchTextPrediction(project, inputUri, outputUri, textModel, location);
  }

  // Perform batch text prediction using a pre-trained text generation model.
  // Example of using Google Cloud Storage bucket as the input and output data source
  static BatchPredictionJob batchTextPrediction(
      String projectId, String inputUri, String outputUri, String textModel, String location)
      throws IOException {
    BatchPredictionJob response;
    JobServiceSettings jobServiceSettings =  JobServiceSettings.newBuilder()
        .setEndpoint("us-central1-aiplatform.googleapis.com:443").build();
    String parent = String.format("projects/%s/locations/%s", projectId, location);
    String modelName = String.format(
        "projects/%s/locations/%s/publishers/google/models/%s", projectId, location, textModel);
    // Construct model parameters
    Map<String, String> modelParameters = new HashMap<>();
    modelParameters.put("maxOutputTokens", "200");
    modelParameters.put("temperature", "0.2");
    modelParameters.put("topP", "0.95");
    modelParameters.put("topK", "40");
    Value parameterValue = mapToValue(modelParameters);

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (JobServiceClient jobServiceClient = JobServiceClient.create(jobServiceSettings)) {

      BatchPredictionJob batchPredictionJob =
          BatchPredictionJob.newBuilder()
              .setDisplayName("my batch text prediction job " + System.currentTimeMillis())
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
              .setModelParameters(parameterValue)
              .build();

      // Create the batch prediction job
      response =
          jobServiceClient.createBatchPredictionJob(parent, batchPredictionJob);

      System.out.format("response: %s\n", response);
      System.out.format("\tName: %s\n", response.getName());
    }
    return response;
  }

  private static Value mapToValue(Map<String, String> map) throws InvalidProtocolBufferException {
    Gson gson = new Gson();
    String json = gson.toJson(map);
    Value.Builder builder = Value.newBuilder();
    JsonFormat.parser().merge(json, builder);
    return builder.build();
  }
}
// [END generativeaionvertexai_batch_text_predict]
