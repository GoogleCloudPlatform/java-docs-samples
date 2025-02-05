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
 * Responsible for handling the data operations.
 * Handles checking cache first, then database, and updating the cache.
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

    // Use try-catch to avoid missing the database if there's an error with the
    // cache
    try {
      // Check if the data exists in the cache first
      String cachedValue = jedis.get(idString);
      if (cachedValue != null) {
        // Return the cached data
        Item cachedItem = Item.fromJsonString(cachedValue);
        cachedItem.setFromCache(true);
        return cachedItem;
      }
    } catch (Exception e) {
      // If there's an error with the cache, log the error and continue
      System.err.println("Error with cache: " + e.getMessage());
    }

    Optional<Item> item = itemsRepository.get(id);

    if (item.isEmpty()) {
      // If the data doesn't exist in the database, return null
      return null;
    }

    // Use try-catch to avoid missing returning the data if there's an error with
    // the cache
    try {
      // Cache result from the database with the default TTL
      jedis.setex(idString, DEFAULT_TTL, item.get().toJsonObject().toString());
    } catch (Exception e) {
      // If there's an error with the cache, log the error and continue
      System.err.println("Error with cache: " + e.getMessage());
    }

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
    Item createdItem = new Item(
        itemId,
        item.getName(),
        item.getDescription(),
        item.getPrice());

    // Use try-catch to avoid returning the data if there's an error with the cache
    try {
      // Cache the data with the default TTL
      String idString = Long.toString(itemId);
      jedis.setex(idString, DEFAULT_TTL, createdItem.toJsonObject().toString());
    } catch (Exception e) {
      // If there's an error with the cache, log the error and continue
      System.err.println("Error with cache: " + e.getMessage());
    }

    return itemId;
  }

  public void delete(long id) {
    // Delete the data from database
    itemsRepository.delete(id);

    // Use try-catch to avoid missing the cache if there's an error with the cache
    try {
      // Also, delete the data from the cache if it exists
      String idString = Long.toString(id);
      long totalDeleted = jedis.del(idString);

      if (totalDeleted == 0) {
        throw new Exception("Item not found in cache");
      }
    } catch (Exception e) {
      // If there's an error with the cache, log the error and continue
      System.err.println("Error with cache: " + e.getMessage());
    }
  }

  public boolean exists(long id) {
    String idString = Long.toString(id);

    // Use try-catch to avoid missing the database if there's an error with the
    // cache
    boolean cacheExists = false;
    try {
      // Check if the data exists in the cache
      cacheExists = jedis.exists(idString);
    } catch (Exception e) {
      // If there's an error with the cache, log the error and continue
      System.err.println("Error with cache: " + e.getMessage());
    }

    // Check if the data exists in the cache or the database (check the cache first)
    return cacheExists || itemsRepository.exists(id);
  }
}
