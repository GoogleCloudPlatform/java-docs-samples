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

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public final class MemorystoreReadItem {

    /** Replace the instance ID of the Memorystore instance. */
    private static final String INSTANCE_ID = "INSTANCE_ID";

    /** If valid, replace the port number of the Memorystore instance. */
    private static final int PORT = 6379;

    /** Replace, the ID of the item to delete from the cache. */
    private static final String ITEM_ID = "ITEM_ID";

    private MemorystoreReadItem() {
        // No-op; won't be called
    }

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
            }

            // Print the cached item not found
            if (cachedItem == null) {
                System.out.printf("No cached item found: %s%n", ITEM_ID);
            }
        }
    }
}
