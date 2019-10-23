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

import com.google.cloud.translate.v3beta1.GetSupportedLanguagesRequest;
import com.google.cloud.translate.v3beta1.SupportedLanguage;
import com.google.cloud.translate.v3beta1.SupportedLanguages;
import com.google.cloud.translate.v3beta1.TranslationServiceClient;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class TranslateV3GetSupportedLanguagesForTarget {
  // [START translate_v3_get_supported_languages_for_target]
  /*
   * Please include the following imports to run this sample.
   *
   * import com.google.cloud.translate.v3.GetSupportedLanguagesRequest;
   * import com.google.cloud.translate.v3.SupportedLanguage;
   * import com.google.cloud.translate.v3.SupportedLanguages;
   * import com.google.cloud.translate.v3.TranslationServiceClient;
   */

  /** Listing supported languages with target language name */
  public static void sampleGetSupportedLanguages(String languageCode, String projectId) {
    try (TranslationServiceClient translationServiceClient = TranslationServiceClient.create()) {
      // TODO(developer): Uncomment and set the following variables
      // languageCode = "en";
      // projectId = "[Google Cloud Project ID]";
      String formattedParent = TranslationServiceClient.formatLocationName(projectId, "global");
      GetSupportedLanguagesRequest request =
          GetSupportedLanguagesRequest.newBuilder()
              .setDisplayLanguageCode(languageCode)
              .setParent(formattedParent)
              .build();
      SupportedLanguages response = translationServiceClient.getSupportedLanguages(request);
      // List language codes of supported languages
      for (SupportedLanguage language : response.getLanguagesList()) {
        System.out.printf("Language Code: %s\n", language.getLanguageCode());
        System.out.printf("Display Name: %s\n", language.getDisplayName());
      }
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
  }
  // [END translate_v3_get_supported_languages_for_target]

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("language_code").build());
    options.addOption(Option.builder("")
            .required(false)
            .hasArg(true)
            .longOpt("project_id")
            .build());

    CommandLine cl = (new DefaultParser()).parse(options, args);
    String languageCode = cl.getOptionValue("language_code", "en");
    String projectId = cl.getOptionValue("project_id", "[Google Cloud Project ID]");

    sampleGetSupportedLanguages(languageCode, projectId);
  }
}
