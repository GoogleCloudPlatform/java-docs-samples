/*
 * Copyright 2020 Google LLC
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

package com.example.functions;

// [START functions_firebase_auth]
import com.google.cloud.functions.Context;
import com.google.cloud.functions.RawBackgroundFunction;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.logging.Logger;

public class FirebaseAnalytics implements RawBackgroundFunction {

  // Use GSON (https://github.com/google/gson) to parse JSON content.
  private Gson gsonParser = new Gson();

  private static final Logger LOGGER = Logger.getLogger(FirebaseAnalytics.class.getName());

  @Override
  public void accept(String json, Context context) {
    JsonObject body = gsonParser.fromJson(json, JsonObject.class);

    LOGGER.info("Function triggered by event: " + context.resource());

    if (body != null && body.has("eventDim")) {
      JsonObject analyticsEvent = body.get("eventDim").getAsJsonObject();
      LOGGER.info("Name: " + analyticsEvent.get("name").getAsString());
      LOGGER.info("Timestamp: " + analyticsEvent.get("timestamp").getAsString());
    }

    if (body != null && body.has("userDim")) {
      JsonObject userObj = body.get("userDim").getAsJsonObject();
      JsonObject geoObj = userObj.get("geoInfo").getAsJsonObject();

      JsonObject deviceInfo = userObj.get("deviceInfo").getAsJsonObject();
      LOGGER.info("Device Model: " + deviceInfo.get("deviceModel").getAsString());

      String city = geoObj.get("city").getAsString();
      String country = geoObj.get("country").getAsString();
      LOGGER.info(String.format("Location: %s, %s", city, country));
    }
  }
}

// [END functions_firebase_auth]
