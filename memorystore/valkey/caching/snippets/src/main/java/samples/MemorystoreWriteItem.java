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
 * <p>Prerequisites: 1. A running Memorystore for Redis instance in Google Cloud.
 *
 * <p>Replace "INSTANCE_ID" with the private IP of your Memorystore instance. Replace "ITEM_ID" and
 * "ITEM_VALUE" with the key and value to be cached.
 */
import redis.clients.jedis.Jedis;

public class MemorystoreWriteItem {

  public static void main(String[] args) {
    /** Connect to the Memorystore Redis instance */
    Jedis jedis = new Jedis("127.0.0.1", 6379);

    /** Replace with the item ID and value to cache */
    String itemId = "foo";
    String itemValue = "bar";

    /** Write the item to the cache */
    jedis.set(itemId, itemValue);

    /** Print out the cached result */
    System.out.println("Item cached with ID: " + itemId + " and value: " + itemValue);

    /** Close the connection */
    jedis.close();
  }
}
