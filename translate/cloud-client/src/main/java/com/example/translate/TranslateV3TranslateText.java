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

public class TranslateV3TranslateText {
  // [START translate_v3_translate_text]
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
   * Translating Text
   *
   * @param text The content to translate in string format
   * @param targetLanguage Required. The BCP-47 language code to use for translation.
   */
  public static void sampleTranslateText(String text, String targetLanguage, String projectId) {
    try (TranslationServiceClient translationServiceClient = TranslationServiceClient.create()) {
      // TODO(developer): Uncomment and set the following variables
      // text = "Hello, world!";
      // targetLanguage = "fr";
      // project = "[Google Cloud Project ID]";
      List<String> contents = Arrays.asList(text);
      String formattedParent = TranslationServiceClient.formatLocationName(projectId, "global");
      TranslateTextRequest request =
          TranslateTextRequest.newBuilder()
              .addAllContents(contents)
              .setTargetLanguageCode(targetLanguage)
              .setParent(formattedParent)
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
  // [END translate_v3_translate_text]

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("text").build());
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("target_language").build());
    options.addOption(Option.builder("").required(false).hasArg(true)
            .longOpt("project_id").build());

    CommandLine cl = (new DefaultParser()).parse(options, args);
    String text = cl.getOptionValue("text", "Hello, world!");
    String targetLanguage = cl.getOptionValue("target_language", "fr");
    String projectId = cl.getOptionValue("project_id", "[Google Cloud Project ID]");

    sampleTranslateText(text, targetLanguage, projectId);
  }
}
