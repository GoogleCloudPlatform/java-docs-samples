/*
 * Copyright 2022 Google LLC
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
package bigtable.fraud.utils;

import java.util.ArrayList;

public final class UtilFunctions {

  private UtilFunctions() { }

  /**
   * @param input ArrayList of Strings
   * @return comma-seperated string from the input
   */
  public static String arrayListToCommasString(final ArrayList<String> input) {
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < input.size(); i++) {
      if (i == 0) {
        stringBuilder.append(input.get(i));
      } else {
        stringBuilder.append(", ").append(input.get(i));
      }
    }
    return stringBuilder.toString();
  }
}
