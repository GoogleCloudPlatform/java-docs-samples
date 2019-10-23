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

package com.example.translate;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.automl.v1beta1.ModelName;
import com.google.cloud.translate.v3beta1.BatchTranslateMetadata;
import com.google.cloud.translate.v3beta1.BatchTranslateResponse;
import com.google.cloud.translate.v3beta1.BatchTranslateTextRequest;
import com.google.cloud.translate.v3beta1.GcsDestination;
import com.google.cloud.translate.v3beta1.GcsSource;
import com.google.cloud.translate.v3beta1.InputConfig;
import com.google.cloud.translate.v3beta1.OutputConfig;
import com.google.cloud.translate.v3beta1.TranslationServiceClient;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class TranslateV3BatchTranslateTextWithModel {
  // [START translate_v3_batch_translate_text_with_model]
  /*
   * Please include the following imports to run this sample.
   *
   * import com.google.api.gax.longrunning.OperationFuture;
   * import com.google.cloud.translate.v3.BatchTranslateMetadata;
   * import com.google.cloud.translate.v3.BatchTranslateResponse;
   * import com.google.cloud.translate.v3.BatchTranslateTextRequest;
   * import com.google.cloud.translate.v3.GcsDestination;
   * import com.google.cloud.translate.v3.GcsSource;
   * import com.google.cloud.translate.v3.InputConfig;
   * import com.google.cloud.translate.v3.OutputConfig;
   * import com.google.cloud.translate.v3.TranslationServiceClient;
   * import java.util.Arrays;
   * import java.util.HashMap;
   * import java.util.List;
   * import java.util.Map;
   */

  /**
   * Batch translate text using AutoML Translation model
   *
   * @param targetLanguage Required. Specify up to 10 language codes here.
   * @param sourceLanguage Required. Source language code.
   * @param modelId The models to use for translation. Map's key is target language code.
   */
  public static void sampleBatchTranslateTextWithModel(
      String inputUri,
      String outputUri,
      String projectId,
      String location,
      String targetLanguage,
      String sourceLanguage,
      String modelId) {
    try (TranslationServiceClient translationServiceClient = TranslationServiceClient.create()) {
      // TODO(developer): Uncomment and set the following variables
      // inputUri = "gs://cloud-samples-data/text.txt";
      // outputUri = "gs://YOUR_BUCKET_ID/path_to_store_results/";
      // projectId = "[Google Cloud Project ID]";
      // location = "us-central1";
      // targetLanguage = "en";
      // sourceLanguage = "de";
      // modelId = "{your-model-id}";
      List<String> targetLanguageCodes = Arrays.asList(targetLanguage);
      GcsSource gcsSource = GcsSource.newBuilder().setInputUri(inputUri).build();

      // Optional. Can be "text/plain" or "text/html".
      String mimeType = "text/plain";
      InputConfig inputConfigsElement =
          InputConfig.newBuilder().setGcsSource(gcsSource).setMimeType(mimeType).build();
      List<InputConfig> inputConfigs = Arrays.asList(inputConfigsElement);
      GcsDestination gcsDestination =
          GcsDestination.newBuilder().setOutputUriPrefix(outputUri).build();
      OutputConfig outputConfig =
          OutputConfig.newBuilder().setGcsDestination(gcsDestination).build();
      String formattedParent = TranslationServiceClient.formatLocationName(projectId, location);
      Map<String, String> models = new HashMap<>();
      String modelPath = ModelName.format(projectId, location, modelId);

      models.put("ja", modelPath);
      BatchTranslateTextRequest request =
          BatchTranslateTextRequest.newBuilder()
              .setSourceLanguageCode(sourceLanguage)
              .addAllTargetLanguageCodes(targetLanguageCodes)
              .addAllInputConfigs(inputConfigs)
              .setOutputConfig(outputConfig)
              .setParent(formattedParent)
              .putAllModels(models)
              .build();
      OperationFuture<BatchTranslateResponse, BatchTranslateMetadata> future =
          translationServiceClient.batchTranslateTextAsync(request);

      System.out.println("Waiting for operation to complete...");
      BatchTranslateResponse response = future.get();
      // Display the translation for each input text provided
      System.out.printf("Total Characters: %s\n", response.getTotalCharacters());
      System.out.printf("Translated Characters: %s\n", response.getTranslatedCharacters());
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
  }
  // [END translate_v3_batch_translate_text_with_model]

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("input_uri").build());
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("output_uri").build());
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("project_id").build());
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("location").build());
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("target_language").build());
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("source_language").build());
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("model_id").build());

    CommandLine cl = (new DefaultParser()).parse(options, args);
    String inputUri = cl.getOptionValue("input_uri", "gs://cloud-samples-data/text.txt");
    String outputUri =
        cl.getOptionValue("output_uri", "gs://YOUR_BUCKET_ID/path_to_store_results/");
    String projectId = cl.getOptionValue("project_id", "[Google Cloud Project ID]");
    String location = cl.getOptionValue("location", "us-central1");
    String targetLanguage = cl.getOptionValue("target_language", "en");
    String sourceLanguage = cl.getOptionValue("source_language", "de");
    String modelPath =
        cl.getOptionValue(
            "model_id", "{your-model-id}");

    sampleBatchTranslateTextWithModel(
        inputUri, outputUri, projectId, location, targetLanguage, sourceLanguage, modelPath);
  }
}
