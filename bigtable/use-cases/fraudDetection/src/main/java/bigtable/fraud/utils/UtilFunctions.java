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

  /**
   * number of milliseconds in a minute.
   */
  public static final long MINUTE_IN_MILLISECONDS = 60L * 1000;
  /**
   * number of milliseconds in a day.
   */
  public static final long DAY_IN_MILLISECONDS = 24L * 3600 * 1000;
  /**
   * number of milliseconds in a week.
   */
  public static final long WEEK_IN_MILLISECONDS = 7L * 24 * 3600 * 1000;
  /**
   * number of milliseconds in a month.
   */
  public static final long MONTH_IN_MILLISECONDS = 30L * 24 * 3600 * 1000;

  /**
   * number of milliseconds in a minute.
   */

  private UtilFunctions() {
  }

  /**
   * @param lat1 latitude of the first point
   * @param lat2 latitude of the second point
   * @param lon1 longitude of the first point
   * @param lon2 longitude of the second point
   * @return the distance between two points in kilometers.
   */
  public static double distanceKM(final double lat1, final double lat2,
      final double lon1, final double lon2) {

    final int earthRadius = 6371; // Radius of the earth

    double latDistance = Math.toRadians(lat2 - lat1);
    double lonDistance = Math.toRadians(lon2 - lon1);
    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double distance = earthRadius * c;
    distance = Math.pow(distance, 2);
    return Math.sqrt(distance);
  }

  /**
   * @param input ArrayList of Strings
   * @return comma-seperated string from the input
   */
  public static String arrayListToCommasString(final ArrayList<String> input) {
    StringBuilder mlFeaturesStr = new StringBuilder();
    for (int i = 0; i < input.size(); i++) {
      if (i == 0) {
        mlFeaturesStr.append(input.get(i));
      } else {
        mlFeaturesStr.append(", ").append(input.get(i));
      }
    }
    return mlFeaturesStr.toString();
  }
}
