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

package aiplatform.batchpredict;

// [START aiplatform_create_batch_prediction_gemini_job_sample]
import com.google.cloud.aiplatform.util.ValueConverter;
import com.google.cloud.aiplatform.v1.BatchPredictionJob;
import com.google.cloud.aiplatform.v1.GcsDestination;
import com.google.cloud.aiplatform.v1.GcsSource;
import com.google.cloud.aiplatform.v1.JobServiceClient;
import com.google.cloud.aiplatform.v1.JobServiceSettings;
import com.google.cloud.aiplatform.v1.LocationName;
import com.google.protobuf.Value;
import java.io.IOException;

public class CreateBatchPredictionGeminiJobSample {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Update these variables before running the sample.
    String project = "PROJECT_ID";
    String displayName = "my-display-name";
    String modelName = "gemini-1.5-flash-002";
    String instancesFormat = "jsonl";
    String gcsSourceUri =
        "gs://cloud-samples-data/generative-ai/batch/batch_requests_for_multimodal_input.jsonl";
    // Or try "gs://cloud-samples-data/generative-ai/batch/gemini_multimodal_batch_predict.jsonl"
    // for a batch prediction that uses audio, video, and an image.
    String predictionsFormat = "jsonl";
    String gcsDestinationOutputUriPrefix = "gs://MY_BUCKET/";

    createBatchPredictionGeminiJobSample(
        project,
        displayName,
        modelName,
        instancesFormat,
        gcsSourceUri,
        predictionsFormat,
        gcsDestinationOutputUriPrefix);
  }

  public static void createBatchPredictionGeminiJobSample(
      String project,
      String displayName,
      String model,
      String instancesFormat,
      String gcsSourceUri,
      String predictionsFormat,
      String gcsDestinationOutputUriPrefix)
      throws IOException {
    JobServiceSettings settings =
        JobServiceSettings.newBuilder()
            .setEndpoint("us-central1-aiplatform.googleapis.com:443")
            .build();
    String location = "us-central1";

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (JobServiceClient client = JobServiceClient.create(settings)) {
      // Passing in an empty Value object for model parameters;
      // Add model parameters per request in the input jsonl file.
      Value modelParameters = ValueConverter.EMPTY_VALUE;

      GcsSource gcsSource = GcsSource.newBuilder().addUris(gcsSourceUri).build();
      BatchPredictionJob.InputConfig inputConfig =
          BatchPredictionJob.InputConfig.newBuilder()
              .setInstancesFormat(instancesFormat)
              .setGcsSource(gcsSource)
              .build();
      GcsDestination gcsDestination =
          GcsDestination.newBuilder().setOutputUriPrefix(gcsDestinationOutputUriPrefix).build();
      BatchPredictionJob.OutputConfig outputConfig =
          BatchPredictionJob.OutputConfig.newBuilder()
              .setPredictionsFormat(predictionsFormat)
              .setGcsDestination(gcsDestination)
              .build();
      String modelName =
          String.format(
              "projects/%s/locations/%s/publishers/google/models/%s", project, location, model);
      BatchPredictionJob batchPredictionJob =
          BatchPredictionJob.newBuilder()
              .setDisplayName(displayName)
              .setModel(modelName)
              .setModelParameters(modelParameters)
              .setInputConfig(inputConfig)
              .setOutputConfig(outputConfig)
              .build();
      LocationName parent = LocationName.of(project, location);
      BatchPredictionJob response = client.createBatchPredictionJob(parent, batchPredictionJob);
      System.out.format("response: %s\n", response);
      System.out.format("\tName: %s\n", response.getName());
    }
  }
}

// [END aiplatform_create_batch_prediction_gemini_job_sample]
