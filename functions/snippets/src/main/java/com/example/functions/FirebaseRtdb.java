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

// [START functions_firebase_rtdb]
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.cloud.functions.RawBackgroundFunction;
import com.google.cloud.logging.LoggingHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.logging.Logger;

public class FirebaseRtdb implements RawBackgroundFunction {

  // Use GSON (https://github.com/google/gson) to parse JSON content.
  private Gson gsonParser = new Gson();

  private static final Logger LOGGER = Logger.getLogger(FirebaseRtdb.class.getName());

  @Override
  public void accept(String json, Context context) {
    LOGGER.info("Function triggered by change to: " + context.resource());

    JsonObject body = gsonParser.fromJson(json, JsonObject.class);

    boolean isAdmin = false;
    if (body != null && body.has("auth")) {
      JsonObject authObj = body.getAsJsonObject("auth");
      isAdmin = authObj.has("admin") && authObj.get("admin").getAsBoolean();
    }

    LOGGER.info("Admin?: " + isAdmin);

    if (body != null && body.has("delta")) {
      LOGGER.info("Delta:");
      LOGGER.info(body.get("delta").toString());
    }
  }
}

// [END functions_firebase_rtdb]
