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

public enum OrderByType {
    HIGH_TO_LOW("h2l"),
    LOW_TO_HIGH("l2h");

    private final String value;

    OrderByType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static OrderByType fromString(String text) {
        for (OrderByType b : OrderByType.values()) {
            if (b.value.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

    public static boolean isValid(String text) {
        return fromString(text) != null;
    }
}
