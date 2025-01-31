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
 * WITHOUT WARRANTIES OR IMPLIED WARRANTIES OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public final class MemorystoreAddItemToBasket {

    /** Replace the Memorystore instance ID. */
    private static final String INSTANCE_ID = "INSTANCE_ID";

    /** Replace the Memorystore port, if not the default port. */
    private static final int PORT = 6379;

    /** User ID for managing the basket. */
    private static final String USER_ID = "USER_ID";

    /** Item to be added to the user's basket. */
    private static final String ITEM_ID = "ITEM_ID";

    /** Set how many items to be added to the basket. */
    private static final int ITEM_COUNT = 1;

    private MemorystoreAddItemToBasket() {
        // No-op constructor to prevent instantiation
    }

    /**
     * Adds an item to a user's basket in Memorystore.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        // Create a Jedis connection pool
        try (JedisPool pool = new JedisPool(INSTANCE_ID, PORT);
                Jedis jedis = pool.getResource()) {

            String basketKey = "basket:" + USER_ID;

            // Add items to the user's basket
            jedis.hincrBy(basketKey, ITEM_ID, ITEM_COUNT);
            System.out.printf("Added %d items to basket: %s%n", ITEM_COUNT, ITEM_ID);

            // Verify the item is in the basket
            boolean exists = jedis.hexists(basketKey, ITEM_ID);
            if (exists) {
                System.out.printf("Item successfully added: %s%n", ITEM_ID);
            } else {
                System.out.printf("Failed to add item: %s%n", ITEM_ID);
            }
        } catch (Exception e) {
            System.err.printf("Error connecting to Redis: %s%n", e.getMessage());
        }
    }
}
