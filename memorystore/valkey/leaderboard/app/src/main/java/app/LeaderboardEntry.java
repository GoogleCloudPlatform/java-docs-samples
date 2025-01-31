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
