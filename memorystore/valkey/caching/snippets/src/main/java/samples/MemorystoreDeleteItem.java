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

public final class MemorystoreDeleteItem {

    /** Replace the instance ID of the Memorystore instance. */
    private static final String INSTANCE_ID = "INSTANCE_ID";

    /** Replace the Memorystore port, if not the default port. */
    private static final int PORT = 6379;

    /** Replace the id of the item to delete from Memorystore. */
    private static final String ITEM_ID = "ITEM_ID";

    private MemorystoreDeleteItem() {
        // No-op; won't be called
    }

    /**
     * Deletes an item from Memorystore.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {

        // Connect to the Memorystore instance
        JedisPool pool = new JedisPool(INSTANCE_ID, PORT);

        try (Jedis jedis = pool.getResource()) {

            // Attempt to delete the item
            Long result = jedis.del(ITEM_ID);

            // Check if the item was successfully deleted
            if (result > 0) {
                System.out.println("Deleted item: " + ITEM_ID);
            }

            // Print the cached item not found
            if (result == 0) {
                System.out.printf("No cached item found: %s%n", ITEM_ID);
            }
        }
    }
}
