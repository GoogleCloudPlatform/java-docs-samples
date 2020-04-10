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

// [START functions_firebase_reactive]

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.functions.Context;
import com.google.cloud.functions.RawBackgroundFunction;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class FirebaseFirestoreReactive implements RawBackgroundFunction {

  // Use GSON (https://github.com/google/gson) to parse JSON content.
  private Gson GSON_PARSER = new Gson();

  private static final Logger LOGGER = Logger.getLogger(FirebaseFirestoreReactive.class.getName());
  private static final Firestore FIRESTORE = FirestoreOptions.getDefaultInstance().getService();

  @Override
  public void accept(String json, Context context) throws RuntimeException {
    // Get the recently-written value
    JsonObject body = GSON_PARSER.fromJson(json, JsonObject.class);
    JsonObject tempJson = body.getAsJsonObject("value");

    // Verify that value.fields.original.stringValue exists
    String currentValue = null;
    if (tempJson != null) {
      tempJson = tempJson.getAsJsonObject("fields");
    }
    if (tempJson != null) {
      tempJson = tempJson.getAsJsonObject("original");
    }
    if (tempJson != null && tempJson.has("stringValue")) {
      currentValue = tempJson.get("stringValue").getAsString();
    }
    if (currentValue == null) {
      throw new IllegalArgumentException("Malformed JSON");
    }

    // Convert recently-written value to ALL CAPS
    String newValue = currentValue.toUpperCase();

    // Update Firestore DB with ALL CAPS value
    Map<String, String> newFields = new HashMap<>();
    newFields.put("original", newValue);

    String affectedDoc = context.resource().split("/documents/")[1].replace("\"", "");

    LOGGER.info(String.format("Replacing value: %s --> %s", currentValue, newValue));
    try {
      FIRESTORE.document(affectedDoc).set(newFields, SetOptions.merge()).get();
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}

// [END functions_firebase_reactive]
