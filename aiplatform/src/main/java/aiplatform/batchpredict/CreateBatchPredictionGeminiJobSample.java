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

// [START generativeaionvertexai_batch_predict_gemini_createjob_gcs]
import com.google.cloud.aiplatform.v1.BatchPredictionJob;
import com.google.cloud.aiplatform.v1.GcsDestination;
import com.google.cloud.aiplatform.v1.GcsSource;
import com.google.cloud.aiplatform.v1.JobServiceClient;
import com.google.cloud.aiplatform.v1.JobServiceSettings;
import com.google.cloud.aiplatform.v1.LocationName;
import java.io.IOException;

public class CreateBatchPredictionGeminiJobSample {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Update these variables before running the sample.
    String project = "PROJECT_ID";
    String gcsDestinationOutputUriPrefix = "gs://MY_BUCKET/";

    createBatchPredictionGeminiJobSample(project, gcsDestinationOutputUriPrefix);
  }

  // Create a batch prediction job using a JSONL input file and output URI, both in Cloud
  // Storage.
  public static BatchPredictionJob createBatchPredictionGeminiJobSample(
      String project, String gcsDestinationOutputUriPrefix) throws IOException {
    String location = "us-central1";
    JobServiceSettings settings =
        JobServiceSettings.newBuilder()
            .setEndpoint(String.format("%s-aiplatform.googleapis.com:443", location))
            .build();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (JobServiceClient client = JobServiceClient.create(settings)) {
      GcsSource gcsSource =
          GcsSource.newBuilder()
              .addUris(
                  "gs://cloud-samples-data/generative-ai/batch/"
                      + "batch_requests_for_multimodal_input.jsonl")
              // Or try
              // "gs://cloud-samples-data/generative-ai/batch/gemini_multimodal_batch_predict.jsonl"
              // for a batch prediction that uses audio, video, and an image.
              .build();
      BatchPredictionJob.InputConfig inputConfig =
          BatchPredictionJob.InputConfig.newBuilder()
              .setInstancesFormat("jsonl")
              .setGcsSource(gcsSource)
              .build();
      GcsDestination gcsDestination =
          GcsDestination.newBuilder().setOutputUriPrefix(gcsDestinationOutputUriPrefix).build();
      BatchPredictionJob.OutputConfig outputConfig =
          BatchPredictionJob.OutputConfig.newBuilder()
              .setPredictionsFormat("jsonl")
              .setGcsDestination(gcsDestination)
              .build();
      String modelName =
          String.format(
              "projects/%s/locations/%s/publishers/google/models/%s",
              project, location, "gemini-2.5-flash");

      BatchPredictionJob batchPredictionJob =
          BatchPredictionJob.newBuilder()
              .setDisplayName("my-display-name")
              .setModel(modelName) // Add model parameters per request in the input jsonl file.
              .setInputConfig(inputConfig)
              .setOutputConfig(outputConfig)
              .build();

      LocationName parent = LocationName.of(project, location);
      BatchPredictionJob response = client.createBatchPredictionJob(parent, batchPredictionJob);
      System.out.format("\tName: %s\n", response.getName());
      // Example response:
      //   Name: projects/<project>/locations/us-central1/batchPredictionJobs/<job-id>
      return response;
    }
  }
}

// [END generativeaionvertexai_batch_predict_gemini_createjob_gcs]
