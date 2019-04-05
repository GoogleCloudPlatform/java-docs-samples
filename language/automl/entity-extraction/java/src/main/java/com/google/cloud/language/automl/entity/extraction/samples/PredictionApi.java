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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

// Imports the Google Cloud client library
import com.google.cloud.automl.v1beta1.AnnotationPayload;
import com.google.cloud.automl.v1beta1.ExamplePayload;
import com.google.cloud.automl.v1beta1.ModelName;
import com.google.cloud.automl.v1beta1.PredictResponse;
import com.google.cloud.automl.v1beta1.PredictionServiceClient;
import com.google.cloud.automl.v1beta1.TextSnippet;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * Google Cloud AutoML Natural Language Entity API sample application. Example usage: mvn package
 * exec:java
 * -Dexec.mainClass='com.google.cloud.language.automl.entity.extraction.samples.PredictionApi'
 * -Dexec.args='predict [modelId] [path-to-text-file]'
 */
public class PredictionApi {

  // [START automl_natural_language_entity_predict]
  /**
   * Demonstrates using the AutoML client to Extract the text content
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param modelId the Id of the model which will be used for text extraction.
   * @param filePath the Local text file path of the content to be extracted.
   * @throws IOException
   */
  public static void predict(
      String projectId, String computeRegion, String modelId, String filePath) throws IOException {

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
  // [END automl_natural_language_entity_predict]

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

    String projectId = System.getenv("PROJECT_ID");
    String computeRegion = System.getenv("REGION_NAME");

    Namespace ns = null;
    try {
      ns = parser.parseArgs(args);
      if (ns.get("command").equals("predict")) {
        predict(projectId, computeRegion, ns.getString("modelId"), ns.getString("filePath"));
      }
      System.exit(1);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.exit(1);
    }
  }
}
