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

/** Handles CRUD operations for the leaderboard table. */

package app;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class LeaderboardRepository {

  /** Template for database operations. */
  private final JdbcTemplate jdbcTemplate;

  /**
   * Constructs repository with database template.
   *
   * @param newJdbcTemplate database operations template
   */
  public LeaderboardRepository(final JdbcTemplate newJdbcTemplate) {
    this.jdbcTemplate = newJdbcTemplate;
  }

  /**
   * Retrieves all leaderboard entries.
   *
   * @return list of leaderboard entries
   */
  public List<LeaderboardEntry> getEntries() {
    return jdbcTemplate.query(
        "SELECT * FROM leaderboard",
        (rs, rowNum) -> new LeaderboardEntry(
            rs.getString("username"),
            rs.getDouble("score")));
  }

  /**
   * Creates new leaderboard entry.
   *
   * @param username player username
   * @param score  player score
   */
  public void create(final String username, final Double score) {
    jdbcTemplate.update(
        "INSERT INTO leaderboard (username, score) VALUES (?, ?)",
        username, score);
  }

  /**
   * Updates existing leaderboard entry.
   *
   * @param username player username
   * @param score  new score
   */
  public void update(final String username, final Double score) {
    jdbcTemplate.update(
        "UPDATE leaderboard SET score = ? WHERE username = ?",
        score, username);
  }
}
