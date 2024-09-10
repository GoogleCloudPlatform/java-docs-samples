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
import com.google.cloud.aiplatform.v1.JobServiceClient;
import com.google.cloud.aiplatform.v1.JobServiceSettings;
import com.google.cloud.aiplatform.v1.LocationName;
import com.google.cloud.aiplatform.v1.ModelName;
import com.google.cloud.aiplatform.v1.BatchPredictionJob.InputConfig;
import com.google.cloud.aiplatform.v1.BatchPredictionJob.OutputConfig;
import com.google.cloud.aiplatform.v1.GcsSource;
import com.google.cloud.aiplatform.v1.GcsDestination;

import java.io.IOException;

public class BatchTextPredictionSample {

  public static void main(String[] args) throws IOException {
    String project = "YOUR_PROJECT_ID"; // Change to your project ID
    String location = "us-central1"; // Change to your preferred location
    String inputUri = "gs://YOUR_BUCKET/YOUR_INPUT_FILE.jsonl"; // Change to your input file URI
    String outputUri = "gs://YOUR_BUCKET/YOUR_OUTPUT_DIR"; // Change to your output directory URI

    batchTextPrediction(project, location, inputUri, outputUri);
  }

  static BatchPredictionJob batchTextPrediction(
      String project, String location, String inputUri, String outputUri) throws IOException {
    String endpoint = String.format("%s-aiplatform.googleapis.com:443", location);
    JobServiceSettings jobServiceSettings =
        JobServiceSettings.newBuilder().setEndpoint(endpoint).build();

    try (JobServiceClient jobServiceClient = JobServiceClient.create(jobServiceSettings)) {
      String modelId = "text-bison@001"; // Change to your preferred model ID
      ModelName modelName = ModelName.of(project, location, modelId);

      GcsSource.Builder gcsSource = GcsSource.newBuilder();
      gcsSource.addUris(inputUri);
      InputConfig inputConfig =
          InputConfig.newBuilder().setGcsSource(gcsSource).build();

      GcsDestination.Builder gcsDestination = GcsDestination.newBuilder();
      gcsDestination.setOutputUriPrefix(outputUri);
      OutputConfig outputConfig =
          OutputConfig.newBuilder().setGcsDestination(gcsDestination).build();

      BatchPredictionJob.Builder batchPredictionJob =
          BatchPredictionJob.newBuilder()
              .setDisplayName("YOUR_JOB_DISPLAY_NAME") // Change to your preferred display name
              .setModel(modelName.toString())
              .setInputConfig(inputConfig)
              .setOutputConfig(outputConfig);

      LocationName parent = LocationName.of(project, location);
      BatchPredictionJob response =
          jobServiceClient.createBatchPredictionJob(parent, batchPredictionJob.build());

      System.out.println("Batch Prediction Job: " + response.getName());
      System.out.println("State: " + response.getState());

      return response;
    }
  }
}
// [END aiplatform_batch_text_predict]
