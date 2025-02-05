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

public final class Global {

  /** Private constructor to prevent instantiation. */
  private Global() {
    throw new UnsupportedOperationException();
  }

  /** Key for accessing leaderboard entries in responses. */
  public static final String LEADERBOARD_ENTRIES_KEY = "entries";
}
