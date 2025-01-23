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

/**
 * Responsible for handling the data operations. Handles checking cache first, then database, and
 * updating the cache.
 */
package app;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import redis.clients.jedis.Jedis;

@Controller
public class DataController {

  // Default TTL for cached data is 60 seconds (1 minute)
  public static final Long DEFAULT_TTL = 60L;

  private final ItemsRepository itemsRepository;
  private final Jedis jedis;

  public DataController(ItemsRepository cacheRepository, Jedis jedis) {
    this.itemsRepository = cacheRepository;
    this.jedis = jedis;
  }

  public Item get(long id) {
    String idString = Long.toString(id);

    // Check if the data exists in the cache first
    if (jedis.exists(idString)) {
      // If the data exists in the cache extend the TTL
      jedis.expire(idString, DEFAULT_TTL);

      // Return the cached data
      Item cachedItem = Item.fromJSONString(jedis.get(idString));
      cachedItem.setFromCache(true);
      return cachedItem;
    }

    Optional<Item> item = itemsRepository.get(id);

    if (item.isEmpty()) {
      // If the data doesn't exist in the database, return null
      return null;
    }

    // Cache result from the database with the default TTL
    jedis.set(idString, item.get().toJSONObject().toString());
    jedis.expire(idString, DEFAULT_TTL);

    return item.get();
  }

  public List<Item> getMultiple(int amount) {
    // Get multiple items from the database
    return itemsRepository.getMultiple(amount);
  }

  public long create(Item item) {
    // Create the data in the database
    long itemId = itemsRepository.create(item);

    // Clone the item with the generated ID
    Item createdItem = new Item(itemId, item.getName(), item.getDescription(), item.getPrice());

    // Cache the data with the default TTL
    String idString = Long.toString(itemId);
    jedis.set(idString, createdItem.toJSONObject().toString());
    jedis.expire(idString, DEFAULT_TTL);

    return itemId;
  }

  public void delete(long id) {
    // Delete the data from database
    itemsRepository.delete(id);

    // Also, delete the data from the cache if it exists
    String idString = Long.toString(id);
    if (jedis.exists(idString)) {
      jedis.del(idString);
    }
  }

  public boolean exists(long id) {
    String idString = Long.toString(id);

    // Check if the data exists in the cache or the database (check the cache first)
    return jedis.exists(idString) || itemsRepository.exists(id);
  }
}
