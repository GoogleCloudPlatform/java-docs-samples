/** Data class representing a single entry in the leaderboard. */
package app;

public class LeaderboardEntry {

    private final String _username;
    private final Double _score;
    private Long _position;

    public LeaderboardEntry(String username, Double score, Long position) {
        this._username = username;
        this._score = score;
        this._position = position;
    }

    public LeaderboardEntry(String username, Double score) {
        this._username = username;
        this._score = score;
        this._position = -1L;
    }

    public String getUsername() {
        return _username;
    }

    public Double getScore() {
        return _score;
    }

    public Long getPosition() {
        return _position;
    }

    public void setPosition(Long position) {
        _position = position;
    }
}
