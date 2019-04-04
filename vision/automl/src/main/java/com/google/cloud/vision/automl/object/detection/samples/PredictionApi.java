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

package com.google.cloud.vision.automl.object.detection.samples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

// Imports the Google Cloud client library
import com.google.cloud.automl.v1beta1.AnnotationPayload;
import com.google.cloud.automl.v1beta1.ExamplePayload;
import com.google.cloud.automl.v1beta1.Image;
import com.google.cloud.automl.v1beta1.ModelName;
import com.google.cloud.automl.v1beta1.PredictResponse;
import com.google.cloud.automl.v1beta1.PredictionServiceClient;
import com.google.protobuf.ByteString;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
/**
 * Google Cloud AutoML Vision Object Detection API sample application. Example usage: mvn package
 * exec:java
 * -Dexec.mainClass='com.google.cloud.vision.automl.object.detection.samples.PredictionApi'
 * -Dexec.args='predict [modelId] [filePath] [scoreThreshold] '
 */
public class PredictionApi {

  // [START automl_vision_object_detection_predict]
  /**
   * Demonstrates using the AutoML client to detect the object in an image.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param modelId the Id of the model which will be used for image object detection.
   * @param filePath the local text file path of the image to be detected.
   * @param scorethreshold
   * @throws IOException
   */
  public static void predict(
      String projectId,
      String computeRegion,
      String modelId,
      String filePath,
      String scoreThreshold)
      throws IOException {

    // Create client for prediction service.
    PredictionServiceClient predictionClient = PredictionServiceClient.create();

    // Get full path of model.
    ModelName modelName = ModelName.of(projectId, computeRegion, modelId);

    // Read the image and assign to payload.
    ByteString content = ByteString.copyFrom(Files.readAllBytes(Paths.get(filePath)));
    Image image = Image.newBuilder().setImageBytes(content).build();
    ExamplePayload examplePayload = ExamplePayload.newBuilder().setImage(image).build();

    // params is additional domain-specific parameters.
    // scoreThreshold is used to filter the result
    Map<String, String> params = new HashMap<String, String>();
    if (scoreThreshold != null) {
      params.put("score_threshold", scoreThreshold);
    }
    PredictResponse response = predictionClient.predict(modelName, examplePayload, params);

    System.out.println("Prediction results:");
    for (AnnotationPayload annotationPayload : response.getPayloadList()) {
      System.out.println("Predicted class name :" + annotationPayload.getDisplayName());
      System.out.println(
          "Predicted class score :" + annotationPayload.getImageObjectDetection().getScore());
    }
  }
  // [END automl_vision_object_detection_predict]

  public static void main(String[] args) throws IOException {
    PredictionApi predictionApi = new PredictionApi();
    predictionApi.argsHelper(args);
  }

  public void argsHelper(String[] args) throws IOException {
    ArgumentParser parser =
        ArgumentParsers.newFor("PredictionApi")
            .build()
            .defaultHelp(true)
            .description("Prediction API operations.");
    Subparsers subparsers = parser.addSubparsers().dest("command");

    Subparser predictParser = subparsers.addParser("predict");
    predictParser.addArgument("modelId");
    predictParser.addArgument("filePath");
    predictParser.addArgument("scoreThreshold").nargs("?").type(Float.class).setDefault("0.5");

    String projectId = System.getenv("PROJECT_ID");
    String computeRegion = System.getenv("REGION_NAME");

    Namespace ns = null;
    try {
      ns = parser.parseArgs(args);
      if (ns.get("command").equals("predict")) {
        predict(
            projectId,
            computeRegion,
            ns.getString("modelId"),
            ns.getString("filePath"),
            ns.getString("scoreThreshold"));
      }
      System.exit(1);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.exit(1);
    }
  }
}
