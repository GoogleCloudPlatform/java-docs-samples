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

// [START generativeaionvertexai_imagen_edit_image_mask_free]

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EditImageMaskFreeSample {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String inputPath = "/path/to/my-input.png";
    String prompt = ""; // The text prompt describing what you want to see.

    editImageMaskFree(projectId, location, inputPath, prompt);
  }

  // Edit an image without using a mask. The edit is applied to the entire image and is saved to a
  // new file.
  public static PredictResponse editImageMaskFree(
      String projectId, String location, String inputPath, String prompt)
      throws ApiException, IOException {
    final String endpoint = String.format("%s-aiplatform.googleapis.com:443", location);
    PredictionServiceSettings predictionServiceSettings =
        PredictionServiceSettings.newBuilder().setEndpoint(endpoint).build();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (PredictionServiceClient predictionServiceClient =
        PredictionServiceClient.create(predictionServiceSettings)) {

      final EndpointName endpointName =
          EndpointName.ofProjectLocationPublisherModelName(
              projectId, location, "google", "imagegeneration@002");

      // Convert the image to Base64 and create the image map
      String imageBase64 =
          Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(inputPath)));
      Map<String, String> imageMap = new HashMap<>();
      imageMap.put("bytesBase64Encoded", imageBase64);

      Map<String, Object> instancesMap = new HashMap<>();
      instancesMap.put("prompt", prompt); // [ "prompt", "<my-prompt>" ]
      instancesMap.put(
          "image", imageMap); // [ "image", [ "bytesBase64Encoded", "iVBORw0KGgo...==" ] ]
      Value instances = mapToValue(instancesMap);

      Map<String, Object> paramsMap = new HashMap<>();
      // Optional parameters
      paramsMap.put("seed", 1);
      // Controls the strength of the prompt.
      // 0-9 (low strength), 10-20 (medium strength), 21+ (high strength)
      paramsMap.put("guidanceScale", 21);
      paramsMap.put("sampleCount", 1);
      Value parameters = mapToValue(paramsMap);

      PredictResponse predictResponse =
          predictionServiceClient.predict(
              endpointName, Collections.singletonList(instances), parameters);

      for (Value prediction : predictResponse.getPredictionsList()) {
        Map<String, Value> fieldsMap = prediction.getStructValue().getFieldsMap();
        if (fieldsMap.containsKey("bytesBase64Encoded")) {
          String bytesBase64Encoded = fieldsMap.get("bytesBase64Encoded").getStringValue();
          Path tmpPath = Files.createTempFile("imagen-", ".png");
          Files.write(tmpPath, Base64.getDecoder().decode(bytesBase64Encoded));
          System.out.format("Image file written to: %s\n", tmpPath.toUri());
        }
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

// [END generativeaionvertexai_imagen_edit_image_mask_free]
