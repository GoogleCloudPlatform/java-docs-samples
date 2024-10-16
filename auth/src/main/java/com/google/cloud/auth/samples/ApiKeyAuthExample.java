/*
 * Copyright 2024 Google LLC
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

package com.google.cloud.auth.samples;

// [START auth_cloud_api_key]
import com.google.cloud.language.v2.AnalyzeSentimentResponse;
import com.google.cloud.language.v2.Document;
import com.google.cloud.language.v2.LanguageServiceClient;
import com.google.cloud.language.v2.LanguageServiceSettings;
import java.io.IOException;

// [END auth_cloud_api_key]

/**
 * Demonstrate how to authenticate requests using an API Key using the Language API as an example.
 */
public class ApiKeyAuthExample {

  // [START auth_cloud_api_key]
  static void authApiKey(String apiKey) throws IOException {
    LanguageServiceSettings settings =
        LanguageServiceSettings.newBuilder().setApiKey(apiKey).build();
    LanguageServiceClient client = LanguageServiceClient.create(settings);
    Document document =
        Document.newBuilder().setContent("Hello World!").setType(Document.Type.PLAIN_TEXT).build();

    AnalyzeSentimentResponse actualResponse = client.analyzeSentiment(document);

    System.out.println(actualResponse.getDocumentSentiment().toString());

    client.close();
  }
  // [END auth_cloud_api_keys]

  public static void main(String[] args) throws IOException {
    if (args.length >= 1) {
      authApiKey(args[0]);
    } else {
      throw new IllegalArgumentException("Api key is required for this test");
    }
  }
}
