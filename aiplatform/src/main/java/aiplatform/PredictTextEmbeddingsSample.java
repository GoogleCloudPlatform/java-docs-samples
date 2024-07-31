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
// [START generativeaionvertexai_sdk_embedding]
import static java.util.stream.Collectors.toList;

import com.google.cloud.aiplatform.v1.EndpointName;
import com.google.cloud.aiplatform.v1.PredictRequest;
import com.google.cloud.aiplatform.v1.PredictResponse;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1.PredictionServiceSettings;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PredictTextEmbeddingsSample {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // Details about text embedding request structure and supported models are available in:
    // https://cloud.google.com/vertex-ai/docs/generative-ai/embeddings/get-text-embeddings
    String endpoint = "us-central1-aiplatform.googleapis.com:443";
    String project = "YOUR_PROJECT_ID";
    String model = "text-embedding-004";
    predictTextEmbeddings(
        endpoint,
        project,
        model,
        List.of("banana bread?", "banana muffins?"),
        "QUESTION_ANSWERING",
        OptionalInt.of(256));
  }

  // Gets text embeddings from a pretrained, foundational model.
  public static List<List<Float>> predictTextEmbeddings(
      String endpoint,
      String project,
      String model,
      List<String> texts,
      String task,
      OptionalInt outputDimensionality)
      throws IOException {
    PredictionServiceSettings settings =
        PredictionServiceSettings.newBuilder().setEndpoint(endpoint).build();
    Matcher matcher = Pattern.compile("^(?<Location>\\w+-\\w+)").matcher(endpoint);
    String location = matcher.matches() ? matcher.group("Location") : "us-central1";
    EndpointName endpointName =
        EndpointName.ofProjectLocationPublisherModelName(project, location, "google", model);

    // You can use this prediction service client for multiple requests.
    try (PredictionServiceClient client = PredictionServiceClient.create(settings)) {
      PredictRequest.Builder request =
          PredictRequest.newBuilder().setEndpoint(endpointName.toString());
      if (outputDimensionality.isPresent()) {
        request.setParameters(
            Value.newBuilder()
                .setStructValue(
                    Struct.newBuilder()
                        .putFields("outputDimensionality", valueOf(outputDimensionality.getAsInt()))
                        .build()));
      }
      for (int i = 0; i < texts.size(); i++) {
        request.addInstances(
            Value.newBuilder()
                .setStructValue(
                    Struct.newBuilder()
                        .putFields("content", valueOf(texts.get(i)))
                        .putFields("task_type", valueOf(task))
                        .build()));
      }
      PredictResponse response = client.predict(request.build());
      List<List<Float>> floats = new ArrayList<>();
      for (Value prediction : response.getPredictionsList()) {
        Value embeddings = prediction.getStructValue().getFieldsOrThrow("embeddings");
        Value values = embeddings.getStructValue().getFieldsOrThrow("values");
        floats.add(
            values.getListValue().getValuesList().stream()
                .map(Value::getNumberValue)
                .map(Double::floatValue)
                .collect(toList()));
      }
      return floats;
    }
  }

  private static Value valueOf(String s) {
    return Value.newBuilder().setStringValue(s).build();
  }

  private static Value valueOf(int n) {
    return Value.newBuilder().setNumberValue(n).build();
  }
}
// [END aiplatform_sdk_embedding]
// [END generativeaionvertexai_sdk_embedding]
