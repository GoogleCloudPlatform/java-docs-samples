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

// [START aiplatform_batch_code_predict]

import com.google.cloud.aiplatform.v1.BatchPredictionJob;
import com.google.cloud.aiplatform.v1.GcsDestination;
import com.google.cloud.aiplatform.v1.GcsSource;
import com.google.cloud.aiplatform.v1.JobServiceClient;
import com.google.cloud.aiplatform.v1.JobServiceSettings;
import com.google.cloud.aiplatform.v1.LocationName;
import java.io.IOException;

public class BatchCodePredictionSample {

  public static void main(String[] args) throws IOException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    String project = "YOUR_PROJECT_ID";
    String location = "us-central1";
    String gcsSourceUri = "gs://cloud-samples-data/batch/prompt_for_batch_code_predict.jsonl";
    String gcsDestinationOutputUriPrefix = "gs://YOUR_BUCKET/batch_code_predict_output";
    String modelId = "code-bison";

    batchCodePredictionSample(project, location, gcsSourceUri,
        gcsDestinationOutputUriPrefix, modelId);
  }

  //Example of using Google Cloud Storage bucket as the input and output data source
  public static void batchCodePredictionSample(
      String project, String location, String gcsSourceUri,
      String gcsDestinationOutputUriPrefix, String modelId)
      throws IOException {

    String endpoint = String.format("%s-aiplatform.googleapis.com:443", location);
    LocationName parent = LocationName.of(project, location);
    String modelName = String.format(
        "projects/%s/locations/%s/publishers/google/models/%s", project, location, modelId);

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    JobServiceSettings jobServiceSettings =
        JobServiceSettings.newBuilder().setEndpoint(endpoint).build();
    try (JobServiceClient client = JobServiceClient.create(jobServiceSettings)) {

      GcsSource gcsSource = GcsSource.newBuilder().addUris(gcsSourceUri).build();
      BatchPredictionJob.InputConfig inputConfig =
          com.google.cloud.aiplatform.v1.BatchPredictionJob.InputConfig.newBuilder()
              .setGcsSource(gcsSource)
              .setInstancesFormat("jsonl")
              .build();
      GcsDestination gcsDestination =
          GcsDestination.newBuilder().setOutputUriPrefix(gcsDestinationOutputUriPrefix).build();
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
              .build();

      BatchPredictionJob response = client.createBatchPredictionJob(parent, batchPredictionJob);
      System.out.format("response: %s\n", response);
      System.out.format("\tName: %s\n", response.getName());
    }
  }
}
// [END aiplatform_batch_code_predict]