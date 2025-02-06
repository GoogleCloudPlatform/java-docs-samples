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

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

@Controller
public class DataController {

  /** Repository for persisting leaderboard entries. */
  private final LeaderboardRepository leaderboardRepository;

  /** Redis client for caching leaderboard data. */
  private final Jedis jedis;

  /**
   * Constructs a new DataController.
   *
   * @param redisClient Redis client for caching
   * @param repository  Repository for persistence
   */
  public DataController(final Jedis redisClient,
      final LeaderboardRepository repository) {
    this.leaderboardRepository = repository;
    this.jedis = redisClient;
  }

  /**
   * Get the leaderboard entries starting from the given position.
   *
   * @param position The starting position of the entries to search.
   * @param orderBy  The order of the entries.
   * @param pageSize The number of entries to return.
   * @param username The username to check the rank of.
   * @return The leaderboard entries.
   */
  public LeaderboardResponse getLeaderboard(
      final long position,
      final OrderByType orderBy,
      final long pageSize,
      final String username) {
    String cacheKey = Global.LEADERBOARD_ENTRIES_KEY;
    long maxPosition = position + pageSize - 1;

    // Initialize the cache if it's empty
    boolean cacheUpdated = this.initializeCache();

    // Set the cache status for the front end
    int cacheStatus = cacheUpdated
        ? FromCacheType.FROM_DB.getValue()
        : FromCacheType.FULL_CACHE.getValue();

    // If we have a username, search for the user's rank
    if (username != null) {
      Long userRank = jedis.zrevrank(cacheKey, username);
      if (userRank != null) {
        long pos = userRank;
        long maxPos = userRank + pageSize - 1;

        return new LeaderboardResponse(
            getEntries(cacheKey, pos, maxPos, true),
            cacheStatus);
      }
    }

    // Get the leaderboard entries depending on the order
    List<LeaderboardEntry> leaderboardList = getEntries(
        cacheKey, position, maxPosition,
        orderBy == OrderByType.HIGH_TO_LOW);

    return new LeaderboardResponse(leaderboardList, cacheStatus);
  }

  private List<LeaderboardEntry> getEntries(
      final String cacheKey,
      final long position,
      final long maxPosition,
      final boolean isDescending) {
    // Define an object
    List<Tuple> entries = new ArrayList<>();

    // Use zrevrangeWithScores to get the entries in descending order
    if (isDescending) {
      entries = new ArrayList<>(
          jedis.zrevrangeWithScores(cacheKey, position, maxPosition));
    }

    // If zrangeWithScores is used, the entries are in ascending order
    if (!isDescending) {
      entries = new ArrayList<>(
          jedis.zrangeWithScores(cacheKey, position, maxPosition));
    }

    List<LeaderboardEntry> newEntries = new ArrayList<>();
    for (int i = 0; i < entries.size(); i++) {
      Tuple e = entries.get(i);

      // Calculate overall position
      long overallPosition = position + i;
      if (!isDescending) {
        overallPosition = jedis.zcard(cacheKey) - overallPosition - 1;
      }

      newEntries.add(
          new LeaderboardEntry(
            e.getElement(), e.getScore(), overallPosition
          )
      );
    }

    return newEntries;
  }

  /**
   * Initializes the leaderboard cache if it is empty.
   *
   * @return {@code true} if the cache was initialized, {@code false} if it was
   * already populated.
   */
  private boolean initializeCache() {
    if (this.jedis.zcard(Global.LEADERBOARD_ENTRIES_KEY) > 0) {
      return false;
    }

    List<LeaderboardEntry> entries = this.leaderboardRepository
        .getEntries();

    if (!entries.isEmpty()) {
      for (LeaderboardEntry entry : entries) {
        this.jedis.zadd(
            Global.LEADERBOARD_ENTRIES_KEY,
            entry.getScore(),
            entry.getUsername());
      }
    }

    return true;
  }

  /**
   * Creates or updates a leaderboard entry with the given username and score.
   * Only updates the entry if the new score is higher than the current score.
   *
   * @param username The username of the entry
   * @param score  The score to set
   */
  public void createOrUpdate(final String username, final Double score) {
    // See if score is higher than the current score
    Double currentScore = this.jedis.zscore(
        Global.LEADERBOARD_ENTRIES_KEY, username);
    if (currentScore != null && currentScore >= score) {
      return;
    }

    this.leaderboardRepository.update(username, score);
    this.jedis.zadd(Global.LEADERBOARD_ENTRIES_KEY, score, username);
  }
}
