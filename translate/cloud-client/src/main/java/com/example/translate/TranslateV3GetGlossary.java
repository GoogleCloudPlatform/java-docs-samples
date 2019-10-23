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

import com.google.cloud.translate.v3beta1.GetGlossaryRequest;
import com.google.cloud.translate.v3beta1.Glossary;
import com.google.cloud.translate.v3beta1.TranslationServiceClient;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class TranslateV3GetGlossary {
  // [START translate_v3_get_glossary]
  /*
   * Please include the following imports to run this sample.
   *
   * import com.google.cloud.translate.v3.GetGlossaryRequest;
   * import com.google.cloud.translate.v3.Glossary;
   * import com.google.cloud.translate.v3.TranslationServiceClient;
   */

  /** Get Glossary */
  public static void sampleGetGlossary(String projectId, String glossaryId) {
    try (TranslationServiceClient translationServiceClient = TranslationServiceClient.create()) {
      // TODO(developer): Uncomment and set the following variables
      // projectId = "[Google Cloud Project ID]";
      // glossaryId = "[Glossary ID]";
      String formattedName =
          TranslationServiceClient.formatGlossaryName(projectId, "us-central1", glossaryId);
      GetGlossaryRequest request = GetGlossaryRequest.newBuilder().setName(formattedName).build();
      Glossary response = translationServiceClient.getGlossary(request);
      System.out.printf("Glossary name: %s\n", response.getName());
      System.out.printf("Entry count: %s\n", response.getEntryCount());
      System.out.printf("Input URI: %s\n", response.getInputConfig().getGcsSource().getInputUri());
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
  }
  // [END translate_v3_get_glossary]

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(Option.builder("")
            .required(false)
            .hasArg(true)
            .longOpt("project_id")
            .build());
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("glossary_id").build());

    CommandLine cl = (new DefaultParser()).parse(options, args);
    String projectId = cl.getOptionValue("project_id", "[Google Cloud Project ID]");
    String glossaryId = cl.getOptionValue("glossary_id", "[Glossary ID]");

    sampleGetGlossary(projectId, glossaryId);
  }
}
