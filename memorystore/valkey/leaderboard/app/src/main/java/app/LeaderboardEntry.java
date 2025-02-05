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

/** Data class representing a single entry in the leaderboard. */

package app;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class LeaderboardEntry {

  /** Username for this leaderboard entry. */
  private final String username;

  /** Score achieved by this user. */
  private final Double score;

  /** Position in the leaderboard ranking. */
  private Long position;

  /**
   * Creates a new leaderboard entry with position.
   *
   * @param usernameParam Username for the entry
   * @param scoreParam  Score achieved
   * @param positionParam Position in ranking
   */
  public LeaderboardEntry(final String usernameParam, final Double scoreParam,
      final Long positionParam) {
    this.username = usernameParam;
    this.score = scoreParam;
    this.position = positionParam;
  }

  /**
   * Creates a new leaderboard entry from JSON.
   *
   * @param usernameParam Username for the entry
   * @param scoreParam  Score achieved
   */
  @JsonCreator
  public LeaderboardEntry(
      @JsonProperty("username") final String usernameParam,
      @JsonProperty("score") final Double scoreParam) {
    this.username = usernameParam;
    this.score = scoreParam;
    this.position = -1L;
  }

  /**
   * Gets the username.
   *
   * @return The username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Gets the score.
   *
   * @return The score
   */
  public Double getScore() {
    return score;
  }

  /**
   * Gets the position.
   *
   * @return The position
   */
  public Long getPosition() {
    return position;
  }

  /**
   * Sets the position.
   *
   * @param positionParam The new position
   */
  public void setPosition(final Long positionParam) {
    this.position = positionParam;
  }
}
