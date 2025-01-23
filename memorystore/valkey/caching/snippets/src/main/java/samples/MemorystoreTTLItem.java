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
 * This code snippet is part of a tutorial on how to use Memorystore for Valkey.
 *
 * <p>See https://cloud.google.com/memorystore/docs/valkey/create-instances before running the code
 * snippet.
 *
 * <p>Prerequisites: 1. A running Memorystore for Valkey instance.
 *
 * <p>Replace "INSTANCE_ID" with the private IP of your Memorystore instance. Replace "ITEM_ID" and
 * "ITEM_VALUE" with the key and value of the item to cache.
 */
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class MemorystoreTTLItem {

  /** Configure the Memorystore instance id */
  private static final String instanceId = "INSTANCE_ID";

  /** Configure the Memorystore port, if not the default port */
  private static final int port = 6379;

  /** Configure the id of the item to read/write from Memorystore */
  private static final String itemId = "ITEM_ID";

  /** Configure the value of the item to read/write from Memorystore */
  private static final String itemValue = "ITEM_VALUE";

  /* Run the code snippet */
  public static void main(String[] args) throws InterruptedException {

    /** Connect to your Memorystore for Valkey instance */
    JedisPool pool = new JedisPool(instanceId, port);

    /** Run try with resource */
    try (Jedis jedis = pool.getResource()) {

      /** Set a TTL of 10 seconds during entry creation */
      int ttlSeconds = 10;

      /** Create a new cached item with a TTL value */
      jedis.setex(itemId, ttlSeconds, itemValue);

      /** Print out the cached item details */
      System.out.println(
          "Item cached with ID: "
              + itemId
              + " and value: "
              + itemValue
              + " for "
              + ttlSeconds
              + "s");

      /** Wait for 5 seconds */
      System.out.println("Waiting for 5 seconds...");
      Thread.sleep(5000);

      /** Retrieve the remaining TTL */
      Long remainingTTL = jedis.ttl(itemId);

      /** Find the cached item, and print out the remanining expiry */
      String cachedItem = jedis.get(itemId);

      /** Print out the item id with remaining TTL value */
      System.out.println("Remaining TTL for item " + itemId + ": " + remainingTTL + "s");

      /** Wait for another 6 seconds to demonstrate TTL expiry */
      System.out.println("Waiting for 6 seconds for expiry...");
      Thread.sleep(6000);

      /** Retrieve the remaining TTL or check item existence */
      remainingTTL = jedis.ttl(itemId);

      /** If TTL is less than 0, print a message indicating expiration */
      if (remainingTTL < 0) {
        System.out.println("Item with ID " + itemId + " has expired and is no longer available.");
      }
    }
  }
}
