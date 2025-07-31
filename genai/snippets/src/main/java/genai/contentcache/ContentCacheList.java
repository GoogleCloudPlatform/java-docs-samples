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

// [START googlegenaisdk_contentcache_list]

import com.google.genai.Client;
import com.google.genai.types.CachedContent;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.ListCachedContentsConfig;

public class ContentCacheList {

  public static void main(String[] args) {
    contentCacheList();
  }

  // Lists all cached contents
  public static void contentCacheList() {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      for (CachedContent content : client.caches.list(ListCachedContentsConfig.builder().build())) {
        content.name().ifPresent(name -> System.out.println("Name: " + name));
        content.model().ifPresent(model -> System.out.println("Model: " + model));
        content.updateTime().ifPresent(time -> System.out.println("Last updated at: " + time));
        content.expireTime().ifPresent(time -> System.out.println("Expires at: " + time));
      }
      // Example response:
      // Name: projects/111111111111/locations/us-central1/cachedContents/1111111111111111111
      // Model:
      // projects/111111111111/locations/us-central1/publishers/google/models/gemini-2.5-flash
      // Last updated at: 2025-07-28T21:54:19.125825Z
      // Expires at: 2025-08-04T21:54:18.328233500Z
      // ...
    }
  }
}
// [END googlegenaisdk_contentcache_list]
