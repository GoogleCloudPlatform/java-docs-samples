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

import java.util.List;
import org.json.JSONObject;


/**
 * Response object containing leaderboard entries and cache status.
 */
public final class LeaderboardResponse {

  /** List of entries in the leaderboard. */
  private final List<LeaderboardEntry> entries;

  /** Indicates if the response was served from cache. */
  private final int fromCache;

  /**
   * Constructs a new leaderboard response.
   *
   * @param newEntries   List of leaderboard entries
   * @param newFromCache Cache status indicator
   */
  public LeaderboardResponse(
      final List<LeaderboardEntry> newEntries, final int newFromCache) {
    this.entries = newEntries;
    this.fromCache = newFromCache;
  }

  /**
   * Gets the leaderboard entries.
   *
   * @return List of leaderboard entries
   */
  public List<LeaderboardEntry> getEntries() {
    return entries;
  }

  /**
   * Gets the cache status.
   *
   * @return Cache status indicator
   */
  public int getFromCache() {
    return fromCache;
  }

  /**
   * Converts the response to JSON format.
   *
   * @return JSONObject representation
   */
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("entries", entries);
    json.put("fromCache", fromCache);
    return json;
  }
}
