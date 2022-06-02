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

package com.example.stitcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestUtils {

  // Finds the play URI in the given output.
  public static String getPlayUri(String output) {
    Matcher uriMatcher = Pattern.compile("Play URI: (.*)").matcher(output);
    String playUri = null;
    if (uriMatcher.find()) {
      playUri = uriMatcher.group(1);
    }
    return playUri;
  }

  // Connects to the play URI and returns the renditions information.
  public static String getRenditions(String playUri) throws IOException {
    URL url = new URL(playUri);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    String line;
    String renditions = null;
    while ((line = reader.readLine()) != null) {
      if (line.startsWith("renditions/")) {
        renditions = line;
        break;
      }
    }
    reader.close();
    return renditions;
  }

  // Connects to the renditions URI. This emulates a media player connecting to the API.
  public static void connectToRenditionsUrl(String renditionsUri) throws IOException {
    URL url = new URL(renditionsUri);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.connect();
    connection.getInputStream();
  }
}
