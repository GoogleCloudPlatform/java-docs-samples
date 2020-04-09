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

package com.example.functions.firebase;

// [START functions_firebase_auth]
import com.google.cloud.functions.Context;
import com.google.cloud.functions.RawBackgroundFunction;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.logging.Logger;

public class FirebaseAuth implements RawBackgroundFunction {

  // Use GSON (https://github.com/google/gson) to parse JSON content.
  private Gson gsonParser = new Gson();

  private static final Logger LOGGER = Logger.getLogger(FirebaseAuth.class.getName());

  @Override
  public void accept(String json, Context context) {
    JsonObject body = gsonParser.fromJson(json, JsonObject.class);

    if (body != null && body.has("uid")) {
      LOGGER.info("Function triggered by change to user: " + body.get("uid").getAsString());
    }

    if (body != null && body.has("metadata")) {
      JsonObject metadata = body.get("metadata").getAsJsonObject();
      LOGGER.info("Created at: " + metadata.get("createdAt").getAsString());
    }

    if (body != null && body.has("email")) {
      LOGGER.info("Email: " + body.get("email").getAsString());
    }
  }
}

// [END functions_firebase_auth]
