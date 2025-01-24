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
 * This code snippet demonstrates how to write an item to Google Cloud Memorystore for Redis.
 *
 * <p>For details, see: https://cloud.google.com/memorystore/docs/redis
 *
 * <p>Prerequisites: 1. A running Memorystore for Redis instance. 2. Replace "INSTANCE_ID",
 * "ITEM_ID", and "ITEM_VALUE" with actual values.
 */
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/** Utility class for writing items to Memorystore for Redis. */
public class MemorystoreWriteItem {

    // Memorystore instance configuration
    private static final String INSTANCE_ID = "INSTANCE_ID";
    private static final int PORT = 6379;
    private static final String ITEM_ID = "ITEM_ID";
    private static final String ITEM_VALUE = "ITEM_VALUE";

    /**
     * Writes an item to Memorystore and verifies that it has been successfully written.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {

        // Connect to the Memorystore instance
        JedisPool pool = new JedisPool(INSTANCE_ID, PORT);

        try (Jedis jedis = pool.getResource()) {

            // Write the item to the cache
            System.out.printf(
                    "Writing cached item with ID: %s and value: %s%n", ITEM_ID, ITEM_VALUE);
            jedis.set(ITEM_ID, ITEM_VALUE);

            // Verify the cached item
            System.out.println("Verifying that the item has been successfully written...");
            String cachedItem = jedis.get(ITEM_ID);

            // Print the cached item if found
            if (cachedItem != null) {
                System.out.printf("Found cached item: %s%n", cachedItem);
            } else {
                System.out.printf("No cached item found with ID: %s%n", ITEM_ID);
            }
        }
    }
}
