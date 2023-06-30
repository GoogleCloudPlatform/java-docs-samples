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

// [START aiplatform_sdk_code_completion_test_function]

import com.google.cloud.aiplatform.v1beta1.EndpointName;
import com.google.cloud.aiplatform.v1beta1.PredictResponse;
import com.google.cloud.aiplatform.v1beta1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1beta1.PredictionServiceSettings;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PredictCodeCompletionTestFunctionSample {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace this variable before running the sample.
    String project = "YOUR_PROJECT_ID";

    // Learn how to create prompts to work with a code model to create code completion suggestions:
    // https://cloud.google.com/vertex-ai/docs/generative-ai/code/code-completion-prompts
    String instance =
        "{ \"prefix\": \""
            + "def reverse_string(s):\n"
            + "  return s[::-1]\n"
            + "def test_empty_input_string()"
            + "}";
    String parameters = "{\n" + "  \"temperature\": 0.2,\n" + "  \"maxOutputTokens\": 64,\n" + "}";
    String location = "us-central1";
    String publisher = "google";
    String model = "code-gecko@001";

    predictTestFunction(instance, parameters, project, location, publisher, model);
  }

  // Use Code Completion to complete a test function
  public static void predictTestFunction(
      String instance,
      String parameters,
      String project,
      String location,
      String publisher,
      String model)
      throws IOException {
    final String endpoint = String.format("%s-aiplatform.googleapis.com:443", location);
    PredictionServiceSettings predictionServiceSettings =
        PredictionServiceSettings.newBuilder().setEndpoint(endpoint).build();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (PredictionServiceClient predictionServiceClient =
        PredictionServiceClient.create(predictionServiceSettings)) {
      final EndpointName endpointName =
          EndpointName.ofProjectLocationPublisherModelName(project, location, publisher, model);

      Value instanceValue = stringToValue(instance);
      List<Value> instances = new ArrayList<>();
      instances.add(instanceValue);

      Value parameterValue = stringToValue(parameters);

      PredictResponse predictResponse =
          predictionServiceClient.predict(endpointName, instances, parameterValue);
      System.out.println("Predict Response");
      System.out.println(predictResponse);
    }
  }

  // Convert a Json string to a protobuf.Value
  static Value stringToValue(String value) throws InvalidProtocolBufferException {
    Value.Builder builder = Value.newBuilder();
    JsonFormat.parser().merge(value, builder);
    return builder.build();
  }
}
// [END aiplatform_sdk_code_completion_test_function]
