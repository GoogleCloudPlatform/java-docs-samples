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

// [START generativeaionvertexai_batch_code_predict]

import com.google.cloud.aiplatform.v1.BatchPredictionJob;
import com.google.cloud.aiplatform.v1.GcsDestination;
import com.google.cloud.aiplatform.v1.GcsSource;
import com.google.cloud.aiplatform.v1.JobServiceClient;
import com.google.cloud.aiplatform.v1.JobServiceSettings;
import com.google.cloud.aiplatform.v1.LocationName;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BatchCodePredictionSample {

  public static void main(String[] args) throws IOException, InterruptedException {
    // TODO(developer): Replace the input_uri and outputUri with your own GCS paths
    String project = "YOUR_PROJECT_ID";
    String location = "us-central1";
    // inputUri: URI of the input dataset.
    // Could be a BigQuery table or a Google Cloud Storage file.
    // E.g. "gs://[BUCKET]/[DATASET].jsonl" OR "bq://[PROJECT].[DATASET].[TABLE]"
    String inputUri = "gs://cloud-samples-data/batch/prompt_for_batch_code_predict.jsonl";
    // outputUri: URI where the output will be stored.
    // Could be a BigQuery table or a Google Cloud Storage file.
    // E.g. "gs://[BUCKET]/[OUTPUT].jsonl" OR "bq://[PROJECT].[DATASET].[TABLE]"
    String outputUri = "gs://YOUR_BUCKET/batch_code_predict_output";
    String codeModel = "code-bison";

    batchCodePredictionSample(project, location, inputUri,
        outputUri, codeModel);
  }

  // Perform batch code prediction using a pre-trained code generation model.
  // Example of using Google Cloud Storage bucket as the input and output data source
  public static BatchPredictionJob batchCodePredictionSample(
      String project, String location, String inputUri,
      String outputUri, String modelId)
      throws IOException {

    String endpoint = String.format("%s-aiplatform.googleapis.com:443", location);
    LocationName parent = LocationName.of(project, location);
    String modelName = String.format(
        "projects/%s/locations/%s/publishers/google/models/%s", project, location, modelId);
    JobServiceSettings jobServiceSettings =
        JobServiceSettings.newBuilder().setEndpoint(endpoint).build();
    // Construct your modelParameters
    Map<String, Object> paramsMap = new HashMap<>();
    paramsMap.put("temperature", 0.2);
    paramsMap.put("maxOutputTokens", 200);
    Value parameterValue = mapToValue(paramsMap);

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (JobServiceClient client = JobServiceClient.create(jobServiceSettings)) {

      GcsSource gcsSource = GcsSource.newBuilder().addUris(inputUri).build();
      BatchPredictionJob.InputConfig inputConfig =
          BatchPredictionJob.InputConfig.newBuilder()
              .setGcsSource(gcsSource)
              .setInstancesFormat("jsonl")
              .build();
      GcsDestination gcsDestination =
          GcsDestination.newBuilder()
              .setOutputUriPrefix(outputUri)
              .build();
      BatchPredictionJob.OutputConfig outputConfig =
          BatchPredictionJob.OutputConfig.newBuilder()
              .setGcsDestination(gcsDestination)
              .setPredictionsFormat("jsonl")
              .build();

      BatchPredictionJob batchPredictionJob =
          BatchPredictionJob.newBuilder()
              .setDisplayName("my batch code prediction job" + System.currentTimeMillis())
              .setInputConfig(inputConfig)
              .setModel(modelName)
              .setOutputConfig(outputConfig)
              .setModelParameters(parameterValue)
              .build();
      BatchPredictionJob response = client.createBatchPredictionJob(parent, batchPredictionJob);
      return response;
    }
  }

  private static Value mapToValue(Map<String, Object> map) throws InvalidProtocolBufferException {
    Gson gson = new Gson();
    String json = gson.toJson(map);
    Value.Builder builder = Value.newBuilder();
    JsonFormat.parser().merge(json, builder);
    return builder.build();
  }
}
// [END generativeaionvertexai_batch_code_predict]
