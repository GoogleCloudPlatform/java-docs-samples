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
package Util;

import java.util.ArrayList;

public class UtilFunctions {

  // Constants that will make it easy to read certain timestamps from CBT.
  public static final long DAY_IN_MILLISECONDS = 24L * 3600 * 1000;
  public static final long WEEK_IN_MILLISECONDS = 7L * 24 * 3600 * 1000;
  public static final long MONTH_IN_MILLISECONDS = 30L * 24 * 3600 * 1000;

  // Calculate the distance (KM) between two coordinates.
  public static double distanceKM(double lat1, double lat2, double lon1, double lon2) {

    final int R = 6371; // Radius of the earth

    double latDistance = Math.toRadians(lat2 - lat1);
    double lonDistance = Math.toRadians(lon2 - lon1);
    double a =
        Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(Math.toRadians(lat1))
            * Math.cos(Math.toRadians(lat2))
            * Math.sin(lonDistance / 2)
            * Math.sin(lonDistance / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double distance = R * c;

    distance = Math.pow(distance, 2);
    return Math.sqrt(distance);
  }

  // Given an ArrayList, returns a string with the items seperated by ", "
  public static String arrayListToCommasString(ArrayList<String> mlFeatures) {
    StringBuilder mlFeaturesStr = new StringBuilder();
    for (int i = 0; i < mlFeatures.size(); i++) {
      if (i == 0) {
        mlFeaturesStr.append(mlFeatures.get(i));
      } else {
        mlFeaturesStr.append(", ").append(mlFeatures.get(i));
      }
    }
    return mlFeaturesStr.toString();
  }
}
