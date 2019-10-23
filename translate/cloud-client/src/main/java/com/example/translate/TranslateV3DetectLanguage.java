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

import com.google.cloud.translate.v3beta1.DetectLanguageRequest;
import com.google.cloud.translate.v3beta1.DetectLanguageResponse;
import com.google.cloud.translate.v3beta1.DetectedLanguage;
import com.google.cloud.translate.v3beta1.TranslationServiceClient;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class TranslateV3DetectLanguage {
  // [START translate_v3_detect_language]
  /*
   * Please include the following imports to run this sample.
   *
   * import com.google.cloud.translate.v3.DetectLanguageRequest;
   * import com.google.cloud.translate.v3.DetectLanguageResponse;
   * import com.google.cloud.translate.v3.DetectedLanguage;
   * import com.google.cloud.translate.v3.TranslationServiceClient;
   */

  /**
   * Detecting the language of a text string
   *
   * @param text The text string for performing language detection
   */
  public static void sampleDetectLanguage(String text, String projectId) {
    try (TranslationServiceClient translationServiceClient = TranslationServiceClient.create()) {
      // TODO(developer): Uncomment and set the following variables
      // text = "Hello, world!";
      // projectId = "[Google Cloud Project ID]";
      String formattedParent = TranslationServiceClient.formatLocationName(projectId, "global");

      // Optional. Can be "text/plain" or "text/html".
      String mimeType = "text/plain";
      DetectLanguageRequest request =
          DetectLanguageRequest.newBuilder()
              .setContent(text)
              .setParent(formattedParent)
              .setMimeType(mimeType)
              .build();
      DetectLanguageResponse response = translationServiceClient.detectLanguage(request);
      // Display list of detected languages sorted by detection confidence.
      // The most probable language is first.
      for (DetectedLanguage language : response.getLanguagesList()) {
        // The language detected
        System.out.printf("Language code: %s\n", language.getLanguageCode());
        // Confidence of detection result for this language
        System.out.printf("Confidence: %s\n", language.getConfidence());
      }
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
  }
  // [END translate_v3_detect_language]

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("text").build());
    options.addOption(Option.builder("").required(false).hasArg(true)
            .longOpt("project_id").build());

    CommandLine cl = (new DefaultParser()).parse(options, args);
    String text = cl.getOptionValue("text", "Hello, world!");
    String projectId = cl.getOptionValue("project_id", "[Google Cloud Project ID]");

    sampleDetectLanguage(text, projectId);
  }
}
