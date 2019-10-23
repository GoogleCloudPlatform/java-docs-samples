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
import com.google.cloud.translate.v3beta1.CreateGlossaryMetadata;
import com.google.cloud.translate.v3beta1.CreateGlossaryRequest;
import com.google.cloud.translate.v3beta1.GcsSource;
import com.google.cloud.translate.v3beta1.Glossary;
import com.google.cloud.translate.v3beta1.GlossaryInputConfig;
import com.google.cloud.translate.v3beta1.TranslationServiceClient;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class TranslateV3CreateGlossary {
  // [START translate_v3_create_glossary]
  /*
   * Please include the following imports to run this sample.
   *
   * import com.google.api.gax.longrunning.OperationFuture;
   * import com.google.cloud.translate.v3.CreateGlossaryMetadata;
   * import com.google.cloud.translate.v3.CreateGlossaryRequest;
   * import com.google.cloud.translate.v3.GcsSource;
   * import com.google.cloud.translate.v3.Glossary;
   * import com.google.cloud.translate.v3.Glossary.LanguageCodesSet;
   * import com.google.cloud.translate.v3.GlossaryInputConfig;
   * import com.google.cloud.translate.v3.TranslationServiceClient;
   * import java.util.Arrays;
   * import java.util.List;
   */

  /** Create Glossary */
  public static void sampleCreateGlossary(
      String projectId, String glossaryId, String inputUri) {
    try (TranslationServiceClient translationServiceClient = TranslationServiceClient.create()) {
      // TODO(developer): Uncomment and set the following variables
      // projectId = "[Google Cloud Project ID]";
      // glossaryId = "my_glossary_id_123";
      // inputUri = "gs://translation_samples_beta/glossaries/glossary.csv";
      String formattedParent =
          TranslationServiceClient.formatLocationName(projectId, "us-central1");
      String formattedName =
          TranslationServiceClient.formatGlossaryName(projectId, "us-central1", glossaryId);

      String languageCodesElement = "en";
      String languageCodesElement2 = "ja";
      List<String> languageCodes = Arrays.asList(languageCodesElement, languageCodesElement2);
      Glossary.LanguageCodesSet languageCodesSet =
          Glossary.LanguageCodesSet.newBuilder().addAllLanguageCodes(languageCodes).build();
      GcsSource gcsSource = GcsSource.newBuilder().setInputUri(inputUri).build();
      GlossaryInputConfig inputConfig =
          GlossaryInputConfig.newBuilder().setGcsSource(gcsSource).build();
      Glossary glossary =
          Glossary.newBuilder()
              .setName(formattedName)
              .setLanguageCodesSet(languageCodesSet)
              .setInputConfig(inputConfig)
              .build();
      CreateGlossaryRequest request =
          CreateGlossaryRequest.newBuilder()
              .setParent(formattedParent)
              .setGlossary(glossary)
              .build();
      OperationFuture<Glossary, CreateGlossaryMetadata> future =
          translationServiceClient.createGlossaryAsync(request);

      System.out.println("Waiting for operation to complete...");
      Glossary response = future.get();
      System.out.println("Created Glossary.");
      System.out.printf("Glossary name: %s\n", response.getName());
      System.out.printf("Entry count: %s\n", response.getEntryCount());
      System.out.printf("Input URI: %s\n", response.getInputConfig().getGcsSource().getInputUri());
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
  }
  // [END translate_v3_create_glossary]

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("project_id").build());
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("glossary_id").build());
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("input_uri").build());

    CommandLine cl = (new DefaultParser()).parse(options, args);
    String projectId = cl.getOptionValue("project_id", "[Google Cloud Project ID]");
    String glossaryId = cl.getOptionValue("glossary_id", "my_glossary_id_123");
    String inputUri =
        cl.getOptionValue("input_uri", "gs://translation_samples_beta/glossaries/glossary.csv");

    sampleCreateGlossary(projectId, glossaryId, inputUri);
  }
}
