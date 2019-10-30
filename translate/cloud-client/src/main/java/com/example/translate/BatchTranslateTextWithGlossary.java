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

// [START translate_v3_batch_translate_text_with_glossary]
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.translate.v3.BatchTranslateMetadata;
import com.google.cloud.translate.v3.BatchTranslateResponse;
import com.google.cloud.translate.v3.BatchTranslateTextRequest;
import com.google.cloud.translate.v3.GcsDestination;
import com.google.cloud.translate.v3.GcsSource;
import com.google.cloud.translate.v3.GlossaryName;
import com.google.cloud.translate.v3.InputConfig;
import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.OutputConfig;
import com.google.cloud.translate.v3.TranslateTextGlossaryConfig;
import com.google.cloud.translate.v3.TranslationServiceClient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class BatchTranslateTextWithGlossary {

  public static void batchTranslateTextWithGlossary()
      throws InterruptedException, ExecutionException, IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "[Google Cloud Project ID]";
    String location = "us-central1";
    String sourceLanguage = "en";
    String targetLanguage = "ja";
    String inputUri = "gs://cloud-samples-data/text.txt";
    String outputUri = "gs://YOUR_BUCKET_ID/path_to_store_results/";
    String glossaryId = "[Your Glossary ID]";
    batchTranslateTextWithGlossary(
        projectId, location, sourceLanguage, targetLanguage, inputUri, outputUri, glossaryId);
  }

  // Batch Translate Text with a Glossary.
  public static void batchTranslateTextWithGlossary(
      String projectId,
      String location,
      String sourceLanguage,
      String targetLanguage,
      String inputUri,
      String outputUri,
      String glossaryId)
      throws IOException, ExecutionException, InterruptedException {

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (TranslationServiceClient client = TranslationServiceClient.create()) {
      LocationName parent = LocationName.of(projectId, location);
      GcsSource gcsSource = GcsSource.newBuilder().setInputUri(inputUri).build();
      // Optional. Can be "text/plain" or "text/html".
      InputConfig inputConfig =
          InputConfig.newBuilder().setGcsSource(gcsSource).setMimeType("text/plain").build();
      GcsDestination gcsDestination =
          GcsDestination.newBuilder().setOutputUriPrefix(outputUri).build();
      OutputConfig outputConfig =
          OutputConfig.newBuilder().setGcsDestination(gcsDestination).build();
      GlossaryName glossaryName = GlossaryName.of(projectId, location, glossaryId);
      TranslateTextGlossaryConfig glossaryConfig =
          TranslateTextGlossaryConfig.newBuilder().setGlossary(glossaryName.toString()).build();
      BatchTranslateTextRequest request =
          BatchTranslateTextRequest.newBuilder()
              .setParent(parent.toString())
              .setSourceLanguageCode(sourceLanguage)
              .addTargetLanguageCodes(targetLanguage)
              .addInputConfigs(inputConfig)
              .setOutputConfig(outputConfig)
              .putGlossaries("ja", glossaryConfig)
              .build();

      OperationFuture<BatchTranslateResponse, BatchTranslateMetadata> future =
          client.batchTranslateTextAsync(request);

      System.out.println("Waiting for operation to complete...");
      BatchTranslateResponse response = future.get();
      // Display the translation for each input text provided
      System.out.printf("Total Characters: %s\n", response.getTotalCharacters());
      System.out.printf("Translated Characters: %s\n", response.getTranslatedCharacters());
    }
  }
}
// [END translate_v3_batch_translate_text_with_glossary]
