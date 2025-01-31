/** Handles CRUD operations for the leaderboard table. */
package app;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LeaderboardRepository {

    private final JdbcTemplate jdbcTemplate;

    public LeaderboardRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<LeaderboardEntry> getEntries() {
        return jdbcTemplate.query(
                "SELECT * FROM leaderboard",
                (rs, rowNum) ->
                        new LeaderboardEntry(rs.getString("username"), rs.getDouble("score")));
    }

    public void create(String username, Double score) {
        jdbcTemplate.update(
                "INSERT INTO leaderboard (username, score) VALUES (?, ?)", username, score);
    }

    public void update(String username, Double score) {
        jdbcTemplate.update("UPDATE leaderboard SET score = ? WHERE username = ?", score, username);
    }
}
