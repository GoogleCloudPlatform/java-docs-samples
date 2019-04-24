/*
 * Copyright 2019 Google LLC
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

package com.google.cloud.language.automl.entity.extraction.samples;

// [START automl_natural_language_entity_predict]

import com.google.cloud.automl.v1beta1.AnnotationPayload;
import com.google.cloud.automl.v1beta1.ExamplePayload;
import com.google.cloud.automl.v1beta1.ModelName;
import com.google.cloud.automl.v1beta1.PredictResponse;
import com.google.cloud.automl.v1beta1.PredictionServiceClient;
import com.google.cloud.automl.v1beta1.TextSnippet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

class Predict {

  static void predict(String projectId, String computeRegion, String modelId, String filePath)
      throws IOException {
    // String projectId = "YOUR_PROJECT_ID";
    // String computeRegion = "YOUR_COMPUTE_REGION";
    // String filePath = "LOCAL_PATH_FOR_TEXT_FILE";

    // Create client for prediction service.
    PredictionServiceClient predictionClient = PredictionServiceClient.create();

    // Get full path of model
    ModelName modelName = ModelName.of(projectId, computeRegion, modelId);

    // Read the file content for prediction.
    String content = new String(Files.readAllBytes(Paths.get(filePath)));

    // Set the payload by giving the content and type of the file.
    TextSnippet textSnippet =
        TextSnippet.newBuilder().setContent(content).setMimeType("text/plain").build();
    ExamplePayload payload = ExamplePayload.newBuilder().setTextSnippet(textSnippet).build();

    // params is additional domain-specific parameters.
    // currently there is no additional parameters supported.
    Map<String, String> params = new HashMap<String, String>();
    PredictResponse response = predictionClient.predict(modelName, payload, params);

    System.out.println("Prediction results:");
    for (AnnotationPayload annotationPayload : response.getPayloadList()) {
      System.out.println(
          "Predicted Text Extract Entity Type :" + annotationPayload.getDisplayName());
      System.out.println(
          "Predicted Text Extract Entity Content :"
              + annotationPayload.getTextExtraction().getTextSegment().getContent());
      System.out.println(
          "Predicted Text Start Offset :"
              + annotationPayload.getTextExtraction().getTextSegment().getStartOffset());
      System.out.println(
          "Predicted Text End Offset :"
              + annotationPayload.getTextExtraction().getTextSegment().getEndOffset());
      System.out.println(
          "Predicted Text Score :" + annotationPayload.getTextExtraction().getScore());
    }
  }
}
// [END automl_natural_language_entity_predict]
