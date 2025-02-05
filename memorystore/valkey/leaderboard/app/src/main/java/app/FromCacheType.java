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

/** Cache state indicator for leaderboard data. */
public enum FromCacheType {
  /** Data freshly loaded from database. */
  FROM_DB(0),
  /** Data served from cache. */
  FULL_CACHE(1);

  /** Numeric value for the cache state. */
  private final int value;

  /**
   * Constructor.
   *
   * @param valueParam Numeric value for this state
   */
  FromCacheType(final int valueParam) {
    this.value = valueParam;
  }

  /**
   * Gets the numeric value.
   *
   * @return The source of the data that has been returned.
   * A database source will return 0. 
   * A caching source will return 1. 
   */
  public int getValue() {
    return value;
  }
}
