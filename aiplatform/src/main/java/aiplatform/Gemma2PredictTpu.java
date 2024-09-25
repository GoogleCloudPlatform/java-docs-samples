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

// [START generativeaionvertexai_gemma2_predict_tpu]

import com.google.cloud.aiplatform.v1.EndpointName;
import com.google.cloud.aiplatform.v1.PredictResponse;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1.PredictionServiceSettings;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Gemma2PredictTpu {
  private final PredictionServiceClient predictionServiceClient;

  // Constructor to inject the PredictionServiceClient
  public Gemma2PredictTpu(PredictionServiceClient predictionServiceClient) {
    this.predictionServiceClient = predictionServiceClient;
  }

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "YOUR_PROJECT_ID";
    String endpointRegion = "us-west1";
    String endpointId = "YOUR_ENDPOINT_ID";
    String parameters =
        "{\n"
            + "  \"temperature\": 0.9,\n"
            + "  \"maxOutputTokens\": 1024,\n"
            + "  \"topP\": 1.0,\n"
            + "  \"topK\": 1\n"
            + "}";

    PredictionServiceSettings predictionServiceSettings =
        PredictionServiceSettings.newBuilder()
            .setEndpoint(String.format("%s-aiplatform.googleapis.com:443", endpointRegion))
            .build();
    PredictionServiceClient predictionServiceClient =
        PredictionServiceClient.create(predictionServiceSettings);
    Gemma2PredictTpu creator = new Gemma2PredictTpu(predictionServiceClient);

    creator.gemma2PredictTpu(projectId, endpointRegion, endpointId, parameters);
  }

  // Demonstrates how to run interference on a Gemma2 model
  // deployed to a Vertex AI endpoint with TPU accelerators.
  public String gemma2PredictTpu(String projectId, String region,
           String endpointId, String parameters) throws IOException {
    // Prompt used in the prediction
    String instance = "{ \"prompt\": \"Why is the sky blue?\"}";

    Value.Builder parameterValueBuilder = Value.newBuilder();
    JsonFormat.parser().merge(parameters, parameterValueBuilder);
    Value parameterValue = parameterValueBuilder.build();

    Value.Builder instanceValue = Value.newBuilder();
    JsonFormat.parser().merge(instance, instanceValue);

    // Encapsulate the prompt in a correct format for TPUs
    // Example format: [{'prompt': 'Why is the sky blue?', 'parameters': {'temperature': 0.8}}]
    List<Value> instances = new ArrayList<>();
    instances.add(instanceValue.build());

    EndpointName endpointName = EndpointName.of(projectId, region, endpointId);

    PredictResponse predictResponse = this.predictionServiceClient
        .predict(endpointName, instances, parameterValue);
    String textResponse = predictResponse.getPredictions(0).getStringValue();
    System.out.println(textResponse);
    return textResponse;
  }
}
// [END generativeaionvertexai_gemma2_predict_tpu]

