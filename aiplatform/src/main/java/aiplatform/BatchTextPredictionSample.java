/*
  * Copyright 2024 Google LLC
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package aiplatform;

// [START aiplatform_batch_text_predict]

import com.google.cloud.aiplatform.v1.BatchPredictionJob;
import com.google.cloud.aiplatform.v1.BatchPredictionJob.InputConfig;
import com.google.cloud.aiplatform.v1.BatchPredictionJob.OutputConfig;
import com.google.cloud.aiplatform.v1.GcsDestination;
import com.google.cloud.aiplatform.v1.GcsSource;
import com.google.cloud.aiplatform.v1.JobServiceClient;
import com.google.cloud.aiplatform.v1.JobServiceSettings;
import com.google.cloud.aiplatform.v1.LocationName;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;


public class BatchTextPredictionSample {

  public static void main(String[] args) throws IOException {
    // TODO (Developer): Replace the input_uri and output_uri with your own GCS paths
    String project = "YOUR_PROJECT_ID";
    String location = "us-central1";
    // input_uri (str, optional): URI of the input dataset.
    // Could be a BigQuery table or a Google Cloud Storage file.
    // E.g. "gs://[BUCKET]/[DATASET].jsonl" OR "bq://[PROJECT].[DATASET].[TABLE]"
    String inputUri = "gs://cloud-samples-data/batch/prompt_for_batch_text_predict.jsonl";
    // outputUri (str, optional): URI where the output will be stored.
    // Could be a BigQuery table or a Google Cloud Storage file.
    // E.g. "gs://[BUCKET]/[OUTPUT].jsonl" OR "bq://[PROJECT].[DATASET].[TABLE]"
    String outputUri = "gs://YOUR_BUCKET/batch_text_predict_output";
    String codeModel = "text-bison";

    batchTextPrediction(project, location, inputUri, outputUri, codeModel);
  }

  // Perform batch text prediction using a pre-trained text generation model.
  // Example of using Google Cloud Storage bucket as the input and output data source
  public static void batchTextPrediction(
      String project, String location, String inputUri,
      String outputUri, String codeModel) throws IOException {
    String endpoint = String.format("%s-aiplatform.googleapis.com:443", location);
    LocationName parent = LocationName.of(project, location);
    String modelName = String.format(
        "projects/%s/locations/%s/publishers/google/models/%s", project, location, codeModel);
    JobServiceSettings jobServiceSettings =
        JobServiceSettings.newBuilder().setEndpoint(endpoint).build();
    // Construct your modelParameters
    String parameters =
        "{\n" + "  \"temperature\": 0.2,\n" + "  \"maxOutputTokens\": 200\n" + "}";
    Value parameterValue = stringToValue(parameters);

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (JobServiceClient jobServiceClient = JobServiceClient.create(jobServiceSettings)) {

      GcsSource.Builder gcsSource = GcsSource.newBuilder();
      gcsSource.addUris(inputUri);
      InputConfig inputConfig =
          InputConfig.newBuilder()
              .setGcsSource(gcsSource)
              .setInstancesFormat("jsonl")
              .build();

      GcsDestination.Builder gcsDestination = GcsDestination.newBuilder();
      gcsDestination.setOutputUriPrefix(outputUri);
      OutputConfig outputConfig =
          OutputConfig.newBuilder()
              .setGcsDestination(gcsDestination)
              .setPredictionsFormat("jsonl")
              .build();

      BatchPredictionJob batchPredictionJob =
          BatchPredictionJob.newBuilder()
              .setDisplayName("my batch text prediction job " + System.currentTimeMillis())
              .setModel(modelName)
              .setInputConfig(inputConfig)
              .setOutputConfig(outputConfig)
              .setModelParameters(parameterValue)
              .build();
      BatchPredictionJob response =
          jobServiceClient.createBatchPredictionJob(parent, batchPredictionJob);

      System.out.format("response: %s\n", response);
      System.out.format("\tName: %s\n", response.getName());
    }
  }

  // Convert a Json string to a protobuf.Value
  static Value stringToValue(String value) throws InvalidProtocolBufferException {
    Value.Builder builder = Value.newBuilder();
    JsonFormat.parser().merge(value, builder);
    return builder.build();
  }
}
// [END aiplatform_batch_text_predict]
