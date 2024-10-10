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

package aiplatform.imagen;

// [START generativeaionvertexai_imagen_get_short_form_image_captions]

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.aiplatform.v1.EndpointName;
import com.google.cloud.aiplatform.v1.PredictResponse;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1.PredictionServiceSettings;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GetShortFormImageCaptionsSample {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String inputPath = "/path/to/my-input.png";

    getShortFormImageCaptions(projectId, location, inputPath);
  }

  // Get the short form captions for an image
  public static PredictResponse getShortFormImageCaptions(
      String projectId, String location, String inputPath) throws ApiException, IOException {
    final String endpoint = String.format("%s-aiplatform.googleapis.com:443", location);
    PredictionServiceSettings predictionServiceSettings =
        PredictionServiceSettings.newBuilder().setEndpoint(endpoint).build();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (PredictionServiceClient predictionServiceClient =
        PredictionServiceClient.create(predictionServiceSettings)) {

      final EndpointName endpointName =
          EndpointName.ofProjectLocationPublisherModelName(
              projectId, location, "google", "imagetext@001");

      // Encode image to Base64
      String imageBase64 =
          Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(inputPath)));

      // Create the image map
      Map<String, String> imageMap = new HashMap<>();
      imageMap.put("bytesBase64Encoded", imageBase64);

      Map<String, Object> instancesMap = new HashMap<>();
      instancesMap.put("image", imageMap);
      Value instances = mapToValue(instancesMap);

      // Optional parameters
      Map<String, Object> paramsMap = new HashMap<>();
      paramsMap.put("language", "en");
      paramsMap.put("sampleCount", 2);
      Value parameters = mapToValue(paramsMap);

      PredictResponse predictResponse =
          predictionServiceClient.predict(
              endpointName, Collections.singletonList(instances), parameters);

      for (Value prediction : predictResponse.getPredictionsList()) {
        System.out.println(prediction.getStringValue());
      }
      return predictResponse;
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

// [END generativeaionvertexai_imagen_get_short_form_image_captions]
