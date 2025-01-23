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
 * This code snippet is part of a tutorial on how to use Memorystore for Redis.
 *
 * <p>See https://cloud.google.com/memorystore/docs/valkey/create-instances before running the code
 * snippet.
 *
 * <p>Prerequisites: 1. A running Memorystore for Valkey instance.
 *
 * <p>Replace "INSTANCE_ID" with the private IP of your Memorystore instance. Replace "ITEM_ID" and
 * "ITEM_VALUE" with the key and value to be cached.
 */
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class MemorystoreWriteItem {

  /** Configure the Memorystore instance id */
  private static final String instanceId = "INSTANCE_ID";

  /** Configure the Memorystore port, if not the default port */
  private static final int port = 6379;

  /** Configure the id of the item to write to Memorystore */
  private static final String itemId = "ITEM_VALUE";

  /** Configure the id of the item to write to Memorystore */
  private static final String itemValue = "test_three_value";

  /* Run the code snippet */
  public static void main(String[] args) {

    /** Connect to your Memorystore for Valkey instance */
    JedisPool pool = new JedisPool(instanceId, port);

    /** Run try with resource */
    try (Jedis jedis = pool.getResource()) {

      /** Write the item to the cache */
      System.out.println("Writing cached item with ID: " + itemId + " and value: " + itemValue);
      jedis.set(itemId, itemValue);

      /** Read the cached item */
      System.out.println(
          "Check the cached item has been written successfully written to the cache");
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
}
