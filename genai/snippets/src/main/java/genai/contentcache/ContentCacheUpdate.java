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

// [START googlegenaisdk_contentcache_update]

import com.google.genai.Client;
import com.google.genai.types.CachedContent;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.UpdateCachedContentConfig;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ContentCacheUpdate {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    // E.g cacheName = "projects/111111111111/locations/global/cachedContents/1111111111111111111"
    String cacheName = "your-cache-name";
    contentCacheUpdate(cacheName);
  }

  // Updates the cache using the specified cache resource name
  public static void contentCacheUpdate(String cacheName) {

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      // Get info of the cached content
      CachedContent cachedContent = client.caches.get(cacheName, null);

      cachedContent.expireTime()
          .ifPresent(expireTime -> System.out.println("Expire time: " + expireTime));
      // Example response
      // Expire time: 2025-07-29T23:39:49.227291Z

      // Update expire time using TTL
      CachedContent updatedCachedContent =
          client.caches.update(
              cacheName,
              UpdateCachedContentConfig.builder().ttl(Duration.ofSeconds(36000)).build());

      updatedCachedContent.expireTime()
          .ifPresent(expireTime -> System.out.println("Expire time after update: " + expireTime));
      // Example response
      // Expire time after update: 2025-07-30T08:40:33.537205Z

      // Update expire time using specific time stamp
      Instant nextWeek = Instant.now().plus(7, ChronoUnit.DAYS);
      updatedCachedContent =
          client.caches.update(
              cacheName, UpdateCachedContentConfig.builder().expireTime(nextWeek).build());

      updatedCachedContent
          .expireTime()
          .ifPresent(expireTime -> System.out.println("Expire time after update: " + expireTime));
      // Example response
      // Expire time after update: 2025-08-05T22:40:33.713988900Z

      System.out.println("Updated cache: " + cacheName);
    }
  }
}
// [END googlegenaisdk_contentcache_update]
