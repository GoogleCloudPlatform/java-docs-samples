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

package com.google.cloud.language.automl.sentiment.analysis.samples;

// [START automl_natural_language_sentiment_predict]
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
    // String modelId = "YOUR_MODEL_ID";
    // String filePath = "LOCAL_FILE_PATH";

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
    predictionClient.close();
    System.out.println("Prediction results:");
    for (AnnotationPayload annotationPayload : response.getPayloadList()) {
      System.out.println(
          "\tPredicted sentiment label: " + annotationPayload.getTextSentiment().getSentiment());
    }
    System.out.println(
        "\tNormalized sentiment score: " + response.getMetadataOrThrow("sentiment_score"));
  }
}
// [END automl_natural_language_sentiment_predict]
