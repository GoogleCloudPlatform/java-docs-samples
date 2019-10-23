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

import com.google.cloud.translate.v3beta1.TranslateTextGlossaryConfig;
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

public class TranslateV3TranslateTextWithGlossary {
  // [START translate_v3_translate_text_with_glossary]
  /*
   * Please include the following imports to run this sample.
   *
   * import com.google.cloud.translate.v3.TranslateTextGlossaryConfig;
   * import com.google.cloud.translate.v3.TranslateTextRequest;
   * import com.google.cloud.translate.v3.TranslateTextResponse;
   * import com.google.cloud.translate.v3.Translation;
   * import com.google.cloud.translate.v3.TranslationServiceClient;
   * import java.util.Arrays;
   * import java.util.List;
   */

  /**
   * Translates a given text using a glossary.
   *
   * @param text The content to translate in string format
   * @param sourceLanguage Optional. The BCP-47 language code of the input text.
   * @param targetLanguage Required. The BCP-47 language code to use for translation.
   * @param glossaryId Specifies the glossary used for this translation.
   */
  public static void sampleTranslateTextWithGlossary(
      String text,
      String sourceLanguage,
      String targetLanguage,
      String projectId,
      String glossaryId) {
    try (TranslationServiceClient translationServiceClient = TranslationServiceClient.create()) {
      // TODO(developer): Uncomment and set the following variables
      // text = "Hello, world!";
      // sourceLanguage = "en";
      // targetLanguage = "fr";
      // projectId = "[Google Cloud Project ID]";
      // glossaryId = "[YOUR_GLOSSARY_ID]";
      List<String> contents = Arrays.asList(text);
      String formattedParent =
          TranslationServiceClient.formatLocationName(projectId, "us-central1");
      String glossaryPath = TranslationServiceClient
              .formatGlossaryName(projectId, "us-central1", glossaryId);

      TranslateTextGlossaryConfig glossaryConfig =
          TranslateTextGlossaryConfig.newBuilder().setGlossary(glossaryPath).build();

      // Optional. Can be "text/plain" or "text/html".
      String mimeType = "text/plain";
      TranslateTextRequest request =
          TranslateTextRequest.newBuilder()
              .addAllContents(contents)
              .setTargetLanguageCode(targetLanguage)
              .setSourceLanguageCode(sourceLanguage)
              .setParent(formattedParent)
              .setGlossaryConfig(glossaryConfig)
              .setMimeType(mimeType)
              .build();
      TranslateTextResponse response = translationServiceClient.translateText(request);
      // Display the translation for each input text provided
      for (Translation translation : response.getGlossaryTranslationsList()) {
        System.out.printf("Translated text: %s\n", translation.getTranslatedText());
      }
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
  }
  // [END translate_v3_translate_text_with_glossary]

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("text").build());
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("source_language").build());
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("target_language").build());
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("project_id").build());
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("glossary_id").build());

    CommandLine cl = (new DefaultParser()).parse(options, args);
    String text = cl.getOptionValue("text", "Hello, world!");
    String sourceLanguage = cl.getOptionValue("source_language", "en");
    String targetLanguage = cl.getOptionValue("target_language", "fr");
    String projectId = cl.getOptionValue("project_id", "[Google Cloud Project ID]");
    String glossaryId =
        cl.getOptionValue(
            "glossary_id",
            "[YOUR_GLOSSARY_ID]");

    sampleTranslateTextWithGlossary(text, sourceLanguage, targetLanguage, projectId, glossaryId);
  }
}
