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
import com.google.cloud.translate.v3beta1.DeleteGlossaryMetadata;
import com.google.cloud.translate.v3beta1.DeleteGlossaryRequest;
import com.google.cloud.translate.v3beta1.DeleteGlossaryResponse;
import com.google.cloud.translate.v3beta1.TranslationServiceClient;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class TranslateV3DeleteGlossary {
  // [START translate_v3_delete_glossary]
  /*
   * Please include the following imports to run this sample.
   *
   * import com.google.api.gax.longrunning.OperationFuture;
   * import com.google.cloud.translate.v3.DeleteGlossaryMetadata;
   * import com.google.cloud.translate.v3.DeleteGlossaryRequest;
   * import com.google.cloud.translate.v3.DeleteGlossaryResponse;
   * import com.google.cloud.translate.v3.TranslationServiceClient;
   */

  /** Delete Glossary */
  public static void sampleDeleteGlossary(String projectId, String glossaryId) {
    try (TranslationServiceClient translationServiceClient = TranslationServiceClient.create()) {
      // TODO(developer): Uncomment and set the following variables
      // projectId = "[Google Cloud Project ID]";
      // glossaryId = "[Glossary ID]";
      String formattedName =
          TranslationServiceClient.formatGlossaryName(projectId, "us-central1", glossaryId);
      DeleteGlossaryRequest request =
          DeleteGlossaryRequest.newBuilder().setName(formattedName).build();
      OperationFuture<DeleteGlossaryResponse, DeleteGlossaryMetadata> future =
          translationServiceClient.deleteGlossaryAsync(request);

      System.out.println("Waiting for operation to complete...");
      DeleteGlossaryResponse response = future.get();
      System.out.println("Deleted Glossary.");
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
  }
  // [END translate_v3_delete_glossary]

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

    sampleDeleteGlossary(projectId, glossaryId);
  }
}
