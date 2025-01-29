/**
 * Data class representing a single entry in the leaderboard.
 */

package app;

public class LeaderboardEntry {

  private final String _username;
  private final Double _score;

  public LeaderboardEntry(String username, Double score) {
    this._username = username;
    this._score = score;
  }

  public String getUsername() {
    return _username;
  }

  public Double getScore() {
    return _score;
  }
}
