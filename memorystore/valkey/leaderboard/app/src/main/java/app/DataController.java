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

package app;

import org.springframework.stereotype.Controller;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

import java.util.*;
import java.util.ArrayList;
import java.util.List;

@Controller
public class DataController {

    private final LeaderboardRepository leaderboardRepository;
    private final Jedis jedis;

    public DataController(Jedis jedis, LeaderboardRepository leaderboardRepository) {
        this.leaderboardRepository = leaderboardRepository;
        this.jedis = jedis;
    }

    /**
     * Get the leaderboard entries starting from the given position.
     *
     * @param position The starting position of the entries to search.
     * @param orderBy The order of the entries.
     * @param pageSize The number of entries to return.
     * @param username The username to check the rank of.
     * @return The leaderboard entries.
     */
    public LeaderboardResponse getLeaderboard(
            long position, OrderByType orderBy, long pageSize, String username) {
        String cacheKey = Global.LEADERBOARD_ENTRIES_KEY;
        long maxPosition = position + pageSize - 1;

        // Initialize the cache if it's empty
        boolean cacheUpdated = this.initializeCache();

        // Set the cache status for the front end
        int cacheStatus =
                cacheUpdated
                        ? FromCacheType.FROM_DB.getValue()
                        : FromCacheType.FULL_CACHE.getValue();

        // If we have a username, search for the user's rank
        if (username != null) {
            Long userRank = jedis.zrevrank(cacheKey, username);
            if (userRank != null) {
                position = userRank;
                maxPosition = userRank + pageSize - 1;

                return new LeaderboardResponse(
                        getEntries(cacheKey, position, maxPosition, true), cacheStatus);
            }
        }

        // Get the leaderboard entries depending on the order
        List<LeaderboardEntry> leaderboardList =
                getEntries(cacheKey, position, maxPosition, orderBy == OrderByType.HIGH_TO_LOW);

        return new LeaderboardResponse(leaderboardList, cacheStatus);
    }

    private List<LeaderboardEntry> getEntries(
            String cacheKey, long position, long maxPosition, boolean isDescending) {
        // Define an object
        List<Tuple> entries = new ArrayList<>();

        // Use zrevrangeWithScores to get the entries in descending order
        if (isDescending) {
            entries = new ArrayList<>(jedis.zrevrangeWithScores(cacheKey, position, maxPosition));
        }

        // If zrangeWithScores is used, the entries are in ascending order
        if (!isDescending) {
            entries = new ArrayList<>(jedis.zrangeWithScores(cacheKey, position, maxPosition));
        }

        List<LeaderboardEntry> newEntries = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            Tuple e = entries.get(i);
            newEntries.add(new LeaderboardEntry(e.getElement(), e.getScore(), position + i));
        }

        return newEntries;
    }

    private boolean initializeCache() {
        if (this.jedis.zcard(Global.LEADERBOARD_ENTRIES_KEY) > 0) {
            return false;
        }

        List<LeaderboardEntry> entries = this.leaderboardRepository.getEntries();

        if (!entries.isEmpty()) {
            for (LeaderboardEntry entry : entries) {
                this.jedis.zadd(
                        Global.LEADERBOARD_ENTRIES_KEY, entry.getScore(), entry.getUsername());
            }
        }

        return true;
    }

    public void createOrUpdate(String username, Double score) {
        this.leaderboardRepository.update(username, score);
        this.jedis.zadd(Global.LEADERBOARD_ENTRIES_KEY, score, username);
    }
}
