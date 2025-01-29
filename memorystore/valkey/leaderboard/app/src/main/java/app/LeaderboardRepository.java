/**
 * Handles CRUD operations for the leaderboard table.
 */

package app;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LeaderboardRepository {

  private final JdbcTemplate jdbcTemplate;

  public LeaderboardRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<LeaderboardEntry> getEntriesAt(long position, long size) {
    // Query the database for the leaderboard entries in descending order of score
    // For example, if `position` is 0 and `size` is 3, the query should return the top 3 entries, as follows:
    // [ 1. username1 - 100, 2. username2 - 50, 3. username3 - 30 ]
    return jdbcTemplate.query(
      "SELECT * FROM leaderboard ORDER BY score DESC LIMIT ? OFFSET ?",
      (rs, rowNum) ->
        new LeaderboardEntry(rs.getString("username"), rs.getDouble("score")),
      size,
      position
    );
  }

  public void create(String username, Double score) {
    jdbcTemplate.update(
      "INSERT INTO leaderboard (username, score) VALUES (?, ?)",
      username,
      score
    );
  }

  public void update(String username, Double score) {
    jdbcTemplate.update(
      "UPDATE leaderboard SET score = ? WHERE username = ?",
      score,
      username
    );
  }

  public boolean exists(String username) {
    Integer count = jdbcTemplate.queryForObject(
      "SELECT COUNT(*) FROM leaderboard WHERE username = ?",
      Integer.class,
      username
    );
    return count != null && count > 0;
  }

  public Double getScore(String username) {
    try {
      return jdbcTemplate.queryForObject(
        "SELECT score FROM leaderboard WHERE username = ?",
        Double.class,
        username
      );
    } catch (Exception e) {
      // The user does not exist
      return null;
    }
  }
}
