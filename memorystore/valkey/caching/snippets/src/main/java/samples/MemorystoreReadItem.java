/*
 * Copyright 2018 Google LLC
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
 * This code snippet demonstrates how to read an item from Google Cloud Memorystore for Redis.
 *
 * <p>See https://cloud.google.com/memorystore/docs/valkey/create-instances before running the code
 * snippet.
 *
 * <p>Prerequisites: 1. A running Memorystore for Redis instance in Google Cloud. 2. An item already
 * cached in the Redis instance with a known ID.
 *
 * <p>Replace "INSTANCE_ID" with the private IP of your Memorystore instance. Replace "ITEM_ID" with
 * an actual cached item ID.
 */
import redis.clients.jedis.Jedis;

public class MemorystoreReadItem {

  public static void main(String[] args) {
    /** Connect to your Memorystore for Valkey instance */
    Jedis jedis = new Jedis("127.0.0.1", 6379);

    /** Replace with an actual cached item ID */
    String itemId = "foo";

    /** Read the cached item */
    String cachedItem = jedis.get(itemId);

    /* If found, print out the cached item */
    if (cachedItem != null) {
      System.out.println("Cached item: " + cachedItem);
    }

    /** If no item found, print a message */
    if (cachedItem == null) {
      System.out.println("No cached item found with ID: " + itemId);
    }
  }
}
