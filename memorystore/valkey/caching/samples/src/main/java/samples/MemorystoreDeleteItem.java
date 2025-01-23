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
 * <p>Prerequisites: 1. A running Memorystore for Valkey instance in Google Cloud. 2. Your client
 * must run in the same VPC network as the Memorystore instance.
 *
 * <p>Replace "INSTANCE_ID" with the private IP of your Memorystore instance. Replace "ITEM_ID" with
 * the key to be deleted from the cache.
 */
import redis.clients.jedis.Jedis;

public class MemorystoreDeleteItem {

  public static void main(String[] args) {
    /** Connect to your Memorystore for Valkey instance */
    Jedis jedis = new Jedis("127.0.0.1", 6379);

    /** Replace with the item ID to delete from the cache */
    String itemId = "foo";

    /** Delete the item from the cache */
    Long result = jedis.del(itemId);

    /** Check if any items have been successfully deleted */
    if (result > 0) {
      System.out.println("Successfully deleted item with ID: " + itemId);
    }

    /* Print out that no item has been found for deletion */
    if (result == 0) {
      System.out.println("No item found with ID: " + itemId);
    }

    /** Close the Redis connection */
    jedis.close();
  }
}
