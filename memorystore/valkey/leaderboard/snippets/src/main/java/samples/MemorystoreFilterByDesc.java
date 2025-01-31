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

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Set;

public final class MemorystoreFilterByDesc {

    /** Replace the Memorystore instance id. */
    private static final String INSTANCE_ID = "INSTANCE_ID";

    /** Replace the Memorystore port, if not the default port. */
    private static final int PORT = 6379;

    /** Set the name for the Leaderboard */
    private static final String LEADERBOARD_KEY = "leaderboard";

    /** Replace the names and scores to write to Memorystore. */
    private static final List<SimpleEntry<String, Double>> USER_SCORES =
            List.of(
                    new SimpleEntry<>("User1", 100.0),
                    new SimpleEntry<>("User2", 80.0),
                    new SimpleEntry<>("User3", 95.0),
                    new SimpleEntry<>("User4", 70.0));

    private MemorystoreFilterByDesc() {
        // No-op; won't be called
    }

    /**
     * Writes to Memorystore and retrieves the leaderboard sorted in descending order.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        // Connect to the Memorystore instance
        JedisPool pool = new JedisPool(INSTANCE_ID, PORT);

        try (Jedis jedis = pool.getResource()) {
            // Add the scores to the leaderboard
            for (SimpleEntry<String, Double> entry : USER_SCORES) {
                jedis.zadd(LEADERBOARD_KEY, entry.getKey(), user.value());
                System.out.printf("Added/Updated %s with score %s%n", user, score);
            }

            // Retrieve and print all users sorted by score in descending order
            System.out.println("\nLeaderboard (Sorted by Descending Scores):");
            Set<String> sortedUsers = jedis.zrevrange(LEADERBOARD_KEY, 0, -1);

            // Print the leaderboard in descending order
            for (String user : sortedUsers) {
                System.out.printf(
                        "User: %s, Score: %s%n", user, jedis.zscore(LEADERBOARD_KEY, user));
            }

            // Get the highest-ranked user
            System.out.println("\nTop Ranked User:");
            Set<String> topUser = jedis.zrevrange(LEADERBOARD_KEY, 0, 0);
            if (!topUser.isEmpty()) {
                String user = topUser.iterator().next();
                System.out.printf(
                        "User: %s, Score: %s%n", user, jedis.zscore(LEADERBOARD_KEY, user));
            } else {
                System.out.println("Leaderboard is empty.");
            }
        } catch (Exception e) {
            System.err.printf("Error connecting to Redis: %s%n", e.getMessage());
        }
    }
}
