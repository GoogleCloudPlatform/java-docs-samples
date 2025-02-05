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

/** Enum for specifying sort order of leaderboard entries. */
public enum OrderByType {
  /** Sort from highest to lowest score. */
  HIGH_TO_LOW("h2l"),
  /** Sort from lowest to highest score. */
  LOW_TO_HIGH("l2h");

  /** String value representing the sort order. */
  private final String value;

  /**
   * Constructor.
   *
   * @param valueParam String value for this order type
   */
  OrderByType(final String valueParam) {
    this.value = valueParam;
  }

  /**
   * Gets the string value.
   *
   * @return The value for this order type
   */
  public String getValue() {
    return value;
  }

  /**
   * Converts string to enum value.
   *
   * @param text String to convert
   * @return Matching enum value or null if not found
   */
  public static OrderByType fromString(final String text) {
    for (OrderByType b : OrderByType.values()) {
      if (b.value.equalsIgnoreCase(text)) {
        return b;
      }
    }
    return null;
  }

  /**
   * Checks if string matches an enum value.
   *
   * @param text String to check
   * @return True if string matches an enum value
   */
  public static boolean isValid(final String text) {
    return fromString(text) != null;
  }
}
