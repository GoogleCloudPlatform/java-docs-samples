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

public final class MemorystoreLogoutUser {

    /** Replace the Memorystore instance id. */
    private static final String INSTANCE_ID = "INSTANCE_ID";

    /** Replace the Memorystore port, if not the default port. */
    private static final int PORT = 6379;

    /** User ID token for logout */
    private static final String TOKEN = "TOKEN";

    private MemorystoreLogoutUser() {
        // No-op; won't be called
    }

    /**
     * Logs out a user by deleting their session from Memorystore.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        // Connect to the Memorystore instance
        try (JedisPool pool = new JedisPool(INSTANCE_ID, PORT);
                Jedis jedis = pool.getResource()) {

            // Check if the session exists
            if (!jedis.exists(TOKEN)) {
                System.out.printf("User %s is not logged in.%n", TOKEN);
                return;
            }

            // Remove the session from Redis
            jedis.del(TOKEN);
            System.out.printf("User %s has been logged out.%n", TOKEN);
        } catch (Exception e) {
            System.err.printf("Error connecting to Redis: %s%n", e.getMessage());
        }
    }
}
