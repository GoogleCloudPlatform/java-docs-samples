package app;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

@ExtendWith(MockitoExtension.class)
class DataControllerTest {

  @Mock
  private LeaderboardRepository leaderboardRepository;

  @Mock
  private Jedis jedis;

  private DataController dataController;

  @BeforeEach
  void setUp() {
    dataController = new DataController(leaderboardRepository, jedis);
    dataController.cacheTopN = 5;
    dataController.pageSize = 3;
  }

  // ----------------------------------------------------
  // getAt() tests
  // ----------------------------------------------------
  @Nested
  @DisplayName("Testing getAt() method")
  class GetAtTests {

    @Test
    @DisplayName("Should fetch from database only when position >= CACHE_TOP_N")
    void testGetAt_PositionAboveCache() {
      long position = 10; // above the 5-entry cache
      List<LeaderboardEntry> dbEntries = Arrays.asList(
        new LeaderboardEntry("dbUser1", 80.0),
        new LeaderboardEntry("dbUser2", 70.0)
      );

      // Because position >= CACHE_TOP_N, we expect the method to query the DB
      given(
        leaderboardRepository.getEntriesAt(position, dataController.pageSize)
      ).willReturn(dbEntries);

      LeaderboardResponse result = dataController.getAt(position);

      // Verify repository call
      verify(leaderboardRepository).getEntriesAt(
        position,
        dataController.pageSize
      );
      // Verify we didn't call zcard or zrevrangeWithScores
      verify(jedis, never()).zcard(anyString());
      verify(jedis, never()).zrevrangeWithScores(
        anyString(),
        anyLong(),
        anyLong()
      );
      // Check result
      assertEquals(2, result.getEntries().size());
      assertEquals("dbUser1", result.getEntries().get(0).getUsername());
      assertEquals("dbUser2", result.getEntries().get(1).getUsername());
    }

    @Test
    @DisplayName(
      "Should fill cache first if it’s not full, then read from cache when position < CACHE_TOP_N"
    )
    void testGetAt_PositionBelowCache_CacheNotFull() {
      long position = 0; // below the 5-entry cache
      long numEntriesInCache = 2; // only 2 in cache so far
      given(jedis.zcard(Global.LEADERBOARD_VALKEY_KEY)).willReturn(
        numEntriesInCache
      );

      // Database returns enough to fill up the top-5 in cache
      List<LeaderboardEntry> dbEntriesToCache = Arrays.asList(
        new LeaderboardEntry("dbUser1", 200.0),
        new LeaderboardEntry("dbUser2", 180.0),
        new LeaderboardEntry("dbUser3", 150.0)
      );
      given(
        leaderboardRepository.getEntriesAt(
          numEntriesInCache,
          dataController.cacheTopN - numEntriesInCache
        )
      ).willReturn(dbEntriesToCache);

      // After filling the cache, we then read from the cache (zrevrangeWithScores)
      // Let’s return 3 entries from the cache for position=0
      List<Tuple> cachedEntries = new ArrayList<>();
      cachedEntries.add(mockTuple("dbUser1", 200.0));
      cachedEntries.add(mockTuple("dbUser2", 180.0));
      cachedEntries.add(mockTuple("dbUser3", 150.0));
      given(
        jedis.zrevrangeWithScores(Global.LEADERBOARD_VALKEY_KEY, 0, 2)
      ).willReturn(cachedEntries);

      // Finally, we do not need more from DB because PAGE_SIZE=3, and we got 3 from cache
      LeaderboardResponse result = dataController.getAt(position);

      // Verify we filled the cache from the DB
      verify(leaderboardRepository).getEntriesAt(
        numEntriesInCache,
        dataController.cacheTopN - numEntriesInCache
      );
      // Verify that the newly fetched entries are added to the cache
      for (LeaderboardEntry entry : dbEntriesToCache) {
        verify(jedis).zadd(
          Global.LEADERBOARD_VALKEY_KEY,
          entry.getScore(),
          entry.getUsername()
        );
      }

      // Verify cache was read
      verify(jedis).zrevrangeWithScores(Global.LEADERBOARD_VALKEY_KEY, 0, 2);

      // Check result
      assertEquals(3, result.getEntries().size());
      assertEquals("dbUser1", result.getEntries().get(0).getUsername());
      assertEquals(200.0, result.getEntries().get(0).getScore());
    }

    @Test
    @DisplayName(
      "Should use cached entries plus DB if cache entries < PAGE_SIZE"
    )
    void testGetAt_PositionBelowCache_CachePartialAndDB() {
      long position = 2;
      // Suppose cache is full
      given(jedis.zcard(Global.LEADERBOARD_VALKEY_KEY)).willReturn(
        (long) dataController.cacheTopN
      );

      // Return only 1 entry from cache, and we need 2 more from DB
      List<Tuple> cachedEntries = Collections.singletonList(
        mockTuple("cacheUser1", 500.0)
      );
      given(
        jedis.zrevrangeWithScores(Global.LEADERBOARD_VALKEY_KEY, 2, 4)
      ).willReturn(cachedEntries);

      // 2 more entries come from DB
      List<LeaderboardEntry> dbEntries = Arrays.asList(
        new LeaderboardEntry("dbUser2", 400.0),
        new LeaderboardEntry("dbUser3", 300.0)
      );
      given(leaderboardRepository.getEntriesAt(3, 2)).willReturn(dbEntries);

      // Execute
      LeaderboardResponse result = dataController.getAt(position);

      // We expect 3 total (1 from cache, 2 from DB)
      assertEquals(3, result.getEntries().size());
      // 1st is from cache
      assertEquals("cacheUser1", result.getEntries().get(0).getUsername());
      // next 2 from DB
      assertEquals("dbUser2", result.getEntries().get(1).getUsername());
      assertEquals("dbUser3", result.getEntries().get(2).getUsername());
    }
  }

  // ----------------------------------------------------
  // createOrUpdate() tests
  // ----------------------------------------------------
  @Nested
  @DisplayName("Testing createOrUpdate() method")
  class CreateOrUpdateTests {

    @Test
    @DisplayName("Should update cache and DB if user is already in cache")
    void testCreateOrUpdate_UserInCache() {
      // Setup
      String username = "cacheUser";
      double newScore = 600.0;

      // user is in cache => zrank(...) != null
      given(jedis.zrank(Global.LEADERBOARD_VALKEY_KEY, username)).willReturn(
        0L
      );

      // Action
      dataController.createOrUpdate(username, newScore);

      // Verify zadd in the cache
      verify(jedis).zadd(Global.LEADERBOARD_VALKEY_KEY, newScore, username);
      // Verify the repository is updated
      verify(leaderboardRepository, times(2)).update(username, newScore);
      // No creation in DB
      verify(leaderboardRepository, never()).create(anyString(), anyDouble());
    }

    @Test
    @DisplayName("Should create in DB if user is not in cache but exists in DB")
    void testCreateOrUpdate_UserNotInCache_ButExistsInDB() {
      String username = "dbUser";
      double newScore = 250.0;

      // user is NOT in the cache => zrank(...) = null
      given(jedis.zrank(Global.LEADERBOARD_VALKEY_KEY, username)).willReturn(
        null
      );
      // user exists in DB
      given(leaderboardRepository.exists(username)).willReturn(true);

      // Suppose the cache’s lowest user is user5 with score=100.0
      List<String> lowestUserList = Collections.singletonList("user5");
      given(
        jedis.zrange(
          Global.LEADERBOARD_VALKEY_KEY,
          dataController.cacheTopN - 1,
          dataController.cacheTopN - 1
        )
      ).willReturn(lowestUserList);
      given(jedis.zscore(Global.LEADERBOARD_VALKEY_KEY, "user5")).willReturn(
        100.0
      );

      // Action
      dataController.createOrUpdate(username, newScore);

      // user exists => update in DB
      verify(leaderboardRepository).update(username, newScore);

      // Because 250.0 > 100.0, we add user to cache and remove user5
      verify(jedis).zadd(Global.LEADERBOARD_VALKEY_KEY, 250.0, username);
      verify(jedis).zrem(Global.LEADERBOARD_VALKEY_KEY, "user5");
    }

    @Test
    @DisplayName("Should create in DB if user is not in cache nor DB")
    void testCreateOrUpdate_UserNotInCache_NotInDB() {
      String username = "newUser";
      double newScore = 120.0;

      given(jedis.zrank(Global.LEADERBOARD_VALKEY_KEY, username)).willReturn(
        null
      );
      given(leaderboardRepository.exists(username)).willReturn(false);

      // Suppose the cache’s lowest user is userX with score=100.0
      List<String> lowestUserList = Collections.singletonList("userX");
      given(
        jedis.zrange(
          Global.LEADERBOARD_VALKEY_KEY,
          dataController.cacheTopN - 1,
          dataController.cacheTopN - 1
        )
      ).willReturn(lowestUserList);
      given(jedis.zscore(Global.LEADERBOARD_VALKEY_KEY, "userX")).willReturn(
        100.0
      );

      dataController.createOrUpdate(username, newScore);

      // Should create in DB
      verify(leaderboardRepository).create(username, newScore);
      // Because 120.0 > 100.0, add newUser to cache and remove userX
      verify(jedis).zadd(Global.LEADERBOARD_VALKEY_KEY, 120.0, username);
      verify(jedis).zrem(Global.LEADERBOARD_VALKEY_KEY, "userX");
    }

    @Test
    @DisplayName(
      "Should NOT add to cache if new score is not higher than the lowest in the cache"
    )
    void testCreateOrUpdate_LowerThanLowestInCache() {
      String username = "userLow";
      double newScore = 50.0;

      // user not in cache
      given(jedis.zrank(Global.LEADERBOARD_VALKEY_KEY, username)).willReturn(
        null
      );
      // user also doesn't exist in DB
      given(leaderboardRepository.exists(username)).willReturn(false);

      // Suppose lowest user has a score of 100.0
      List<String> lowestUserList = Collections.singletonList("lowestUser");
      given(
        jedis.zrange(
          Global.LEADERBOARD_VALKEY_KEY,
          dataController.cacheTopN - 1,
          dataController.cacheTopN - 1
        )
      ).willReturn(lowestUserList);
      given(
        jedis.zscore(Global.LEADERBOARD_VALKEY_KEY, "lowestUser")
      ).willReturn(100.0);

      dataController.createOrUpdate(username, newScore);

      // create in DB
      verify(leaderboardRepository).create(username, newScore);

      // but do NOT add to cache or remove anything from cache
      verify(jedis, never()).zadd(anyString(), anyDouble(), anyString());
      verify(jedis, never()).zrem(anyString(), anyString());
    }
  }

  // ----------------------------------------------------
  // getScore() tests
  // ----------------------------------------------------
  @Nested
  @DisplayName("Testing getScore() method")
  class GetScoreTests {

    @Test
    @DisplayName("Should return score from cache if user is in cache")
    void testGetScore_UserInCache() {
      String username = "cachedUser";
      double cachedScore = 999.0;

      // zrank(...) != null => in cache
      given(jedis.zrank(Global.LEADERBOARD_VALKEY_KEY, username)).willReturn(
        0L
      );
      // so zscore(...) should be used
      given(jedis.zscore(Global.LEADERBOARD_VALKEY_KEY, username)).willReturn(
        cachedScore
      );

      Double result = dataController.getScore(username);
      assertEquals(cachedScore, result);

      // should never fetch from DB
      verify(leaderboardRepository, never()).getScore(anyString());
    }

    @Test
    @DisplayName("Should return score from DB if user is not in cache")
    void testGetScore_UserNotInCache() {
      String username = "dbUser";
      double dbScore = 300.0;

      // user is not in cache => zrank(...) = null
      given(jedis.zrank(Global.LEADERBOARD_VALKEY_KEY, username)).willReturn(
        null
      );
      // fallback to DB
      given(leaderboardRepository.getScore(username)).willReturn(dbScore);

      Double result = dataController.getScore(username);
      assertEquals(dbScore, result);

      // verify DB usage
      verify(leaderboardRepository).getScore(username);
    }
  }

  // ----------------------------------------------------
  // Utility method to mock Tuple
  // ----------------------------------------------------
  private Tuple mockTuple(String element, double score) {
    Tuple tuple = mock(Tuple.class);
    given(tuple.getElement()).willReturn(element);
    given(tuple.getScore()).willReturn(score);
    return tuple;
  }
}
