/**
 * Responsible for handling the data operations between the API, Valkey, and the database.
 */

package app;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Controller;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

@Controller
public class DataController {

  private final LeaderboardRepository leaderboardRepository;
  private final Jedis jedis;

  public Integer cacheTopN;
  public Integer pageSize;

  public DataController(
    LeaderboardRepository leaderboardRepository,
    Jedis jedis
  ) {
    this.leaderboardRepository = leaderboardRepository;
    this.jedis = jedis;

    this.cacheTopN = Objects.requireNonNullElse(cacheTopN, 100);
    this.pageSize = Objects.requireNonNullElse(pageSize, 25);
  }

  public LeaderboardResponse getAt(long position) {
    // If the requested position is beyond the cache’s topN,
    // skip the cache entirely and use the database.
    if (position >= cacheTopN) {
      return new LeaderboardResponse(
        position,
        leaderboardRepository.getEntriesAt(position, pageSize),
        FromCacheType.FROM_DB.getValue()
      );
    }

    FromCacheType fromCacheType = FromCacheType.FULL_CACHE;

    // If the cache is incomplete (i.e. has fewer than cacheTopN items),
    // top it up from DB before we do any retrieval.
    long numEntriesInCache = jedis.zcard(Global.LEADERBOARD_VALKEY_KEY);
    if (numEntriesInCache < cacheTopN) {
      List<LeaderboardEntry> missingEntries =
        leaderboardRepository.getEntriesAt(
          numEntriesInCache,
          cacheTopN - numEntriesInCache
        );

      // Add the new entries to Redis so we have a full topN in cache.
      for (LeaderboardEntry entry : missingEntries) {
        jedis.zadd(
          Global.LEADERBOARD_VALKEY_KEY,
          entry.getScore(),
          entry.getUsername()
        );
      }

      fromCacheType = FromCacheType.PARTIAL_CACHE;
    }

    // Now fetch from cache if possible.
    List<LeaderboardEntry> leaderboardList = new ArrayList<>();

    // The maximum number of cached entries we *intend* to pull
    // is whichever is smaller: "distance to top of cache" or "pageSize".
    long distance = cacheTopN - position;
    long intendedCachedCount = Math.min(distance, pageSize);

    // Ask Redis for the range [position, position + intendedCachedCount - 1].
    List<Tuple> cachedEntries = jedis.zrevrangeWithScores(
      Global.LEADERBOARD_VALKEY_KEY,
      position,
      position + intendedCachedCount - 1
    );

    // Convert Redis tuples to LeaderboardEntry objects.
    for (Tuple entry : cachedEntries) {
      leaderboardList.add(
        new LeaderboardEntry(entry.getElement(), entry.getScore())
      );
    }

    // If Redis returned fewer items than we *intended* to pull
    // (or if intendedCachedCount < pageSize),
    // we need to fetch from DB to complete this page.
    long actualCachedCount = cachedEntries.size();
    // The total we still need to fill out pageSize is:
    long shortfall = pageSize - actualCachedCount;

    if (shortfall > 0) {
      // The DB call starts from position + actualCachedCount,
      // because we already got `actualCachedCount` from the cache.
      List<LeaderboardEntry> dbEntries = leaderboardRepository.getEntriesAt(
        position + actualCachedCount,
        shortfall
      );
      leaderboardList.addAll(dbEntries);
    }

    return new LeaderboardResponse(
      position,
      leaderboardList,
      fromCacheType.getValue()
    );
  }

  public void createOrUpdate(String username, Double score) {
    if (jedis.zrank(Global.LEADERBOARD_VALKEY_KEY, username) != null) {
      // The user is in the cache, update the score in the cache
      jedis.zadd(Global.LEADERBOARD_VALKEY_KEY, score, username);
      leaderboardRepository.update(username, score);

      // Update the database
      leaderboardRepository.update(username, score);
    } else {
      // Create or update the entry in the database
      if (leaderboardRepository.exists(username)) {
        leaderboardRepository.update(username, score);
      } else {
        leaderboardRepository.create(username, score);
      }

      // Get the lowest score in the cache
      String lowestScoringUser = jedis
        .zrange(Global.LEADERBOARD_VALKEY_KEY, cacheTopN - 1, cacheTopN - 1)
        .get(0);
      Double lowestScore = jedis.zscore(
        Global.LEADERBOARD_VALKEY_KEY,
        lowestScoringUser
      );

      // If the new score is higher than the lowest score in the cache, add it to the cache and remove the lowest score
      if (score > lowestScore) {
        jedis.zadd(Global.LEADERBOARD_VALKEY_KEY, score, username);
        jedis.zrem(Global.LEADERBOARD_VALKEY_KEY, lowestScoringUser);
      }
    }
  }

  public Double getScore(String username) {
    if (jedis.zrank(Global.LEADERBOARD_VALKEY_KEY, username) != null) {
      return jedis.zscore(Global.LEADERBOARD_VALKEY_KEY, username);
    }

    return leaderboardRepository.getScore(username);
  }
}
