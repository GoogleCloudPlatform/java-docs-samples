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
 * This code snippet demonstrates how to use TTL (Time-To-Live) with Google Cloud Memorystore for
 * Redis.
 *
 * <p>For details, see: https://cloud.google.com/memorystore/docs/redis
 *
 * <p>Prerequisites: 1. A running Memorystore for Redis instance. 2. Replace "INSTANCE_ID",
 * "ITEM_ID", and "ITEM_VALUE" with the appropriate values.
 */
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/** Utility class for demonstrating TTL operations with Memorystore for Redis. */
public class MemorystoreTTLItem {

    // Memorystore instance configuration
    private static final String INSTANCE_ID = "INSTANCE_ID";
    private static final int PORT = 6379;
    private static final String ITEM_ID = "ITEM_ID";
    private static final String ITEM_VALUE = "ITEM_VALUE";

    /**
     * Demonstrates creating a cached item with a TTL, checking its expiration, and observing TTL
     * expiry.
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
            Thread.sleep(5000);

            // Retrieve and print the remaining TTL
            Long remainingTTL = jedis.ttl(ITEM_ID);
            System.out.printf("Remaining TTL for item %s: %ds%n", ITEM_ID, remainingTTL);

            // Wait for another 6 seconds to demonstrate TTL expiry
            System.out.println("Waiting for 6 seconds to let the TTL expire...");
            Thread.sleep(6000);

            // Check the remaining TTL and item existence
            remainingTTL = jedis.ttl(ITEM_ID);
            if (remainingTTL < 0) {
                System.out.printf(
                        "Item with ID %s has expired and is no longer available.%n", ITEM_ID);
            } else {
                System.out.printf(
                        "Item with ID %s is still available with TTL: %ds%n",
                        ITEM_ID, remainingTTL);
            }
        }
    }
}
