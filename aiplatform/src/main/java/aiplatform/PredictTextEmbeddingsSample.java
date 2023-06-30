/*
 * Copyright 2023 Google LLC
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

// [START aiplatform_sdk_embedding]

import com.google.cloud.aiplatform.util.ValueConverter;
import com.google.cloud.aiplatform.v1beta1.EndpointName;
import com.google.cloud.aiplatform.v1beta1.PredictResponse;
import com.google.cloud.aiplatform.v1beta1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1beta1.PredictionServiceSettings;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PredictTextEmbeddingsSample {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // Details about text embedding request structure and supported models are available in:
    // https://cloud.google.com/vertex-ai/docs/generative-ai/embeddings/get-text-embeddings
    String instance = "{ \"content\": \"What is life?\"}";
    String project = "YOUR_PROJECT_ID";
    String location = "us-central1";
    String publisher = "google";
    String model = "textembedding-gecko@001";

    predictTextEmbeddings(instance, project, location, publisher, model);
  }

  // Get text embeddings from a supported embedding model
  public static void predictTextEmbeddings(
      String instance, String project, String location, String publisher, String model)
      throws IOException {
    String endpoint = String.format("%s-aiplatform.googleapis.com:443", location);
    PredictionServiceSettings predictionServiceSettings =
        PredictionServiceSettings.newBuilder()
            .setEndpoint(endpoint)
            .build();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (PredictionServiceClient predictionServiceClient =
        PredictionServiceClient.create(predictionServiceSettings)) {
      EndpointName endpointName =
          EndpointName.ofProjectLocationPublisherModelName(project, location, publisher, model);

      // Use Value.Builder to convert instance to a dynamically typed value that can be
      // processed by the service.
      Value.Builder instanceValue = Value.newBuilder();
      JsonFormat.parser().merge(instance, instanceValue);
      List<Value> instances = new ArrayList<>();
      instances.add(instanceValue.build());

      PredictResponse predictResponse =
          predictionServiceClient.predict(endpointName, instances, ValueConverter.EMPTY_VALUE);
      System.out.println("Predict Response");
      for (Value prediction : predictResponse.getPredictionsList()) {
        System.out.format("\tPrediction: %s\n", prediction);
      }
    }
  }
}
// [END aiplatform_sdk_embedding]
