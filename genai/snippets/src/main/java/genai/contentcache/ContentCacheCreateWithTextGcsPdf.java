/*
 * Copyright 2025 Google LLC
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

package genai.contentcache;

// [START googlegenaisdk_contentcache_create_with_txt_gcs_pdf]

import com.google.genai.Client;
import com.google.genai.types.CachedContent;
import com.google.genai.types.Content;
import com.google.genai.types.CreateCachedContentConfig;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;
import java.time.Duration;
import java.util.Optional;

public class ContentCacheCreateWithTextGcsPdf {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    contentCacheCreateWithTextGcsPdf(modelId);
  }

  // Creates a cached content using text and gcs pdfs files
  public static Optional<String> contentCacheCreateWithTextGcsPdf(String modelId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      // Set the system instruction
      Content systemInstruction =
          Content.fromParts(
              Part.fromText(
                  "You are an expert researcher. You always stick to the facts"
                      + " in the sources provided, and never make up new facts.\n"
                      + "Now look at these research papers, and answer the following questions."));

      // Set pdf files
      Content contents =
          Content.fromParts(
              Part.fromUri(
                  "gs://cloud-samples-data/generative-ai/pdf/2312.11805v3.pdf", "application/pdf"),
              Part.fromUri(
                  "gs://cloud-samples-data/generative-ai/pdf/2403.05530.pdf", "application/pdf"));

      // Configuration for cached content using pdfs files and text
      CreateCachedContentConfig config =
          CreateCachedContentConfig.builder()
              .systemInstruction(systemInstruction)
              .contents(contents)
              .displayName("example-cache")
              .ttl(Duration.ofSeconds(86400))
              .build();

      CachedContent cachedContent = client.caches.create(modelId, config);
      cachedContent.name().ifPresent(System.out::println);
      cachedContent.usageMetadata().ifPresent(System.out::println);
      // Example response:
      // projects/111111111111/locations/global/cachedContents/1111111111111111111
      // CachedContentUsageMetadata{audioDurationSeconds=Optional.empty, imageCount=Optional[167],
      // textCount=Optional[153], totalTokenCount=Optional[43125],
      // videoDurationSeconds=Optional.empty}
      return cachedContent.name();
    }
  }
}
// [END googlegenaisdk_contentcache_create_with_txt_gcs_pdf]
