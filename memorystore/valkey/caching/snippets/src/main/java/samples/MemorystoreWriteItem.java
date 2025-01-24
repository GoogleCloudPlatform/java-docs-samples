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

public final class MemorystoreWriteItem {

    /** Replace the Memorystore instance id. */
    private static final String INSTANCE_ID = "INSTANCE_ID";

    /** Replace the Memorystore port, if not the default port. */
    private static final int PORT = 6379;

    /** Replace the id of the item to write to Memorystore. */
    private static final String ITEM_ID = "ITEM_ID";

    /** Replace the id of the item to write to Memorystore. */
    private static final String ITEM_VALUE = "ITEM_VALUE";

    private MemorystoreWriteItem() {
        // No-op; won't be called
    }

    /**
     * Writes to Memorystore before verifying the item exists.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {

        // Connect to the Memorystore instance
        JedisPool pool = new JedisPool(INSTANCE_ID, PORT);

        try (Jedis jedis = pool.getResource()) {

            // Write the item to the cache.
            System.out.printf("Caching item %s %s%n", ITEM_ID, ITEM_VALUE);
            jedis.set(ITEM_ID, ITEM_VALUE);

            // Verify the cached item
            System.out.println("Verifying cache.");
            String cachedItem = jedis.get(ITEM_ID);

            // Print the cached item if found
            if (cachedItem != null) {
                System.out.printf("Found cached item: %s%n", cachedItem);
            }

            // Print the cached item not found
            if (cachedItem == null) {
                System.out.printf("No cached item found: %s%n", ITEM_ID);
            }
        }
    }
}
