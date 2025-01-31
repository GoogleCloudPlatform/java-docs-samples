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

public final class MemorystoreLeaderboardPagination {

    /** Replace the Memorystore instance id. */
    private static final String INSTANCE_ID = "INSTANCE_ID";

    /** Replace the Memorystore port, if not the default port. */
    private static final int PORT = 6379;

    /** Set the name for the Leaderboard */
    private static final String LEADERBOARD_KEY = "leaderboard";

    /** Number of users per page */
    private static final int PAGE_SIZE = 2;

    /** Sample user scores */
    private static final List<SimpleEntry<String, Double>> USER_SCORES =
            List.of(
                    new SimpleEntry<>("User1", 100.0),
                    new SimpleEntry<>("User2", 80.0),
                    new SimpleEntry<>("User3", 95.0),
                    new SimpleEntry<>("User4", 70.0),
                    new SimpleEntry<>("User5", 60.0),
                    new SimpleEntry<>("User6", 50.0));

    private MemorystoreLeaderboardPagination() {
        // No-op; won't be called
    }

    /**
     * Writes to Memorystore and retrieves the leaderboard using pagination.
     *
     * @param args command-line arguments (page number)
     */
    public static void main(final String[] args) {
        // Validate input arguments
        int pageNumber = args.length > 0 ? Integer.parseInt(args[0]) : 1;
        if (pageNumber < 1) {
            System.out.println("Invalid page number. Defaulting to page 1.");
            pageNumber = 1;
        }

        // Connect to the Memorystore instance
        JedisPool pool = new JedisPool(INSTANCE_ID, PORT);

        try (Jedis jedis = pool.getResource()) {
            // Add the scores to the leaderboard
            for (SimpleEntry<String, Double> entry : USER_SCORES) {
                jedis.zadd(LEADERBOARD_KEY, entry.getValue(), entry.getKey());
                System.out.printf(
                        "Added/Updated %s with score %s%n", entry.getKey(), entry.getValue());
            }

            // Display Users on page 1
            System.out.printf("\nLeaderboard - Page %d:\n", 1);
            displayPaginatedLeaderboard(jedis, 1);

            // Display Users on page 2
            System.out.printf("\nLeaderboard - Page %d:\n", 2);
            displayPaginatedLeaderboard(jedis, 2);
        } catch (Exception e) {
            System.err.printf("Error connecting to Redis: %s%n", e.getMessage());
        }
    }

    /**
     * Fetches and displays leaderboard users based on the given page number.
     *
     * @param jedis Redis client instance
     * @param page The page number to fetch
     */
    private static void displayPaginatedLeaderboard(Jedis jedis, int page) {
        // Caulcate the start and end index for each page
        int start = (page - 1) * PAGE_SIZE;
        int end = start + PAGE_SIZE - 1;

        // Use zrevrange to find users between the start and end index
        Set<String> paginatedUsers = jedis.zrevrange(LEADERBOARD_KEY, start, end);

        // If no users are found, print a message and return
        if (paginatedUsers.isEmpty()) {
            System.out.println("No users found on this page.");
            return;
        }

        int ranking = start + 1;
        for (String user : paginatedUsers) {
            System.out.printf(
                    "Rank %d: %s, Score: %s%n",
                    ranking++, user, jedis.zscore(LEADERBOARD_KEY, user));
        }
    }
}
