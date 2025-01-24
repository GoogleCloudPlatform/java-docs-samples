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

public final class MemorystoreTTLItem {

    /** Replace the Memorystore instance id. */
    private static final String INSTANCE_ID = "INSTANCE_ID";

    /** Replace the Memorystore port, if not the default port. */
    private static final int PORT = 6379;

    /** Replace the id of the item to write to Memorystore. */
    private static final String ITEM_ID = "ITEM_ID";

    /** Replace the id of the item to write to Memorystore. */
    private static final String ITEM_VALUE = "ITEM_VALUE";

    /** Set the initial wait time for checking the cache. */
    private static final int INITIAL_WAIT_TIME = 5000;

    /** Set the final wait time for checking the cache. */
    private static final int FINAL_WAIT_TIME = 6000;

    private MemorystoreTTLItem() {
        // No-op; won't be called
    }

    /**
     * Writes to Memorystore with a Time-to-live(TTL) value.
     *
     * @param args command-line arguments
     * @throws InterruptedException if the thread sleep is interrupted
     */
    public static void main(final String[] args) throws InterruptedException {

        // Connect to the Memorystore instance
        JedisPool pool = new JedisPool(INSTANCE_ID, PORT);

        try (Jedis jedis = pool.getResource()) {

            // Set a TTL of 10 seconds during entry creation
            final int ttlSeconds = 10;
            jedis.setex(ITEM_ID, ttlSeconds, ITEM_VALUE);

            // Print out the cached item details
            System.out.printf(
                    "Item cached with ID: %s and value: %s for %ds%n",
                    ITEM_ID, ITEM_VALUE, ttlSeconds);

            // Wait for 5 seconds to demonstrate the TTL countdown
            System.out.println("Waiting for 5 seconds...");
            Thread.sleep(INITIAL_WAIT_TIME);

            // Retrieve and print the remaining TTL
            Long remainingTTL = jedis.ttl(ITEM_ID);
            System.out.printf("Remaining TTL %s: %ds%n", ITEM_ID, remainingTTL);

            // Wait for another 6 seconds to demonstrate TTL expiry
            System.out.println("Waiting for 6s for TTL expiry...");
            Thread.sleep(FINAL_WAIT_TIME);

            // Check the remaining TTL and item existence
            remainingTTL = jedis.ttl(ITEM_ID);
            if (remainingTTL < 0) {
                System.out.printf("Item with ID %s has expired.", ITEM_ID);
            }

            if (remainingTTL >= 0) {
                System.out.printf("Found Item %s", ITEM_ID, remainingTTL);
            }
        }
    }
}
