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
 * This code snippet demonstrates how to read an item from Google Cloud Memorystore for Redis.
 *
 * <p>For details, see: https://cloud.google.com/memorystore/docs/redis
 *
 * <p>Prerequisites: 1. A running Memorystore for Redis instance. 2. An existing item to read from
 * the Memorystore cache.
 *
 * <p>Replace "INSTANCE_ID" with the private IP of your Memorystore instance. Replace "ITEM_ID" with
 * an actual cached item ID.
 */
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/** Utility class for reading items from Memorystore for Redis. */
public class MemorystoreReadItem {

    // Memorystore instance configuration
    private static final String INSTANCE_ID = "INSTANCE_ID";
    private static final int PORT = 6379;
    private static final String ITEM_ID = "ITEM_ID";

    // Private constructor to prevent instantiation
    private MemorystoreReadItem() {}

    /**
     * Reads an item from Memorystore.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {

        // Connect to the Memorystore instance
        JedisPool pool = new JedisPool(INSTANCE_ID, PORT);

        try (Jedis jedis = pool.getResource()) {

            // Attempt to read the cached item
            String cachedItem = jedis.get(ITEM_ID);

            // Print the cached item if found
            if (cachedItem != null) {
                System.out.println("Cached item: " + cachedItem);
            } else {
                System.out.println("No cached item found with ID: " + ITEM_ID);
            }
        }
    }
}
