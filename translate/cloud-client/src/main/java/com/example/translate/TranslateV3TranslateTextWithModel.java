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

import com.google.cloud.automl.v1beta1.ModelName;
import com.google.cloud.translate.v3beta1.TranslateTextRequest;
import com.google.cloud.translate.v3beta1.TranslateTextResponse;
import com.google.cloud.translate.v3beta1.Translation;
import com.google.cloud.translate.v3beta1.TranslationServiceClient;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class TranslateV3TranslateTextWithModel {
  // [START translate_v3_translate_text_with_model]
  /*
   * Please include the following imports to run this sample.
   *
   * import com.google.cloud.translate.v3.TranslateTextRequest;
   * import com.google.cloud.translate.v3.TranslateTextResponse;
   * import com.google.cloud.translate.v3.Translation;
   * import com.google.cloud.translate.v3.TranslationServiceClient;
   * import java.util.Arrays;
   * import java.util.List;
   */

  /**
   * Translating Text with Model
   *
   * @param modelId The `model` type requested for this translation.
   * @param text The content to translate in string format
   * @param targetLanguage Required. The BCP-47 language code to use for translation.
   * @param sourceLanguage Optional. The BCP-47 language code of the input text.
   */
  public static void sampleTranslateTextWithModel(
      String modelId,
      String text,
      String targetLanguage,
      String sourceLanguage,
      String projectId,
      String location) {
    try (TranslationServiceClient translationServiceClient = TranslationServiceClient.create()) {
      // TODO(developer): Uncomment and set the following variables
      // modelId = "[MODEL ID]";
      // text = "Hello, world!";
      // targetLanguage = "fr";
      // sourceLanguage = "en";
      // projectId = "[Google Cloud Project ID]";
      // location = "global";
      List<String> contents = Arrays.asList(text);
      String formattedParent = TranslationServiceClient.formatLocationName(projectId, location);
      String modelPath = ModelName.format(projectId, location, modelId);

      // Optional. Can be "text/plain" or "text/html".
      String mimeType = "text/plain";
      TranslateTextRequest request =
          TranslateTextRequest.newBuilder()
              .addAllContents(contents)
              .setTargetLanguageCode(targetLanguage)
              .setModel(modelPath)
              .setSourceLanguageCode(sourceLanguage)
              .setParent(formattedParent)
              .setMimeType(mimeType)
              .build();
      TranslateTextResponse response = translationServiceClient.translateText(request);
      // Display the translation for each input text provided
      for (Translation translation : response.getTranslationsList()) {
        System.out.printf("Translated text: %s\n", translation.getTranslatedText());
      }
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
  }
  // [END translate_v3_translate_text_with_model]

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("model_id").build());
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("text").build());
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("target_language").build());
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("source_language").build());
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("project_id").build());
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("location").build());

    CommandLine cl = (new DefaultParser()).parse(options, args);
    String modelId =
        cl.getOptionValue(
            "model_id", "[MODEL ID]");
    String text = cl.getOptionValue("text", "Hello, world!");
    String targetLanguage = cl.getOptionValue("target_language", "fr");
    String sourceLanguage = cl.getOptionValue("source_language", "en");
    String projectId = cl.getOptionValue("project_id", "[Google Cloud Project ID]");
    String location = cl.getOptionValue("location", "global");

    sampleTranslateTextWithModel(modelId, text, targetLanguage,
            sourceLanguage, projectId, location);
  }
}
