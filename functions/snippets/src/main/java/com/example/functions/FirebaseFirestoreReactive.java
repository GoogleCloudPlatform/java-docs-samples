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
  private Gson gsonParser = new Gson();

  private static final Logger LOGGER = Logger.getLogger(FirebaseFirestoreReactive.class.getName());
  private static final Firestore firestore = FirestoreOptions.getDefaultInstance().getService();

  @Override
  public void accept(String json, Context context) throws RuntimeException {
    // Get the recently-written value
    JsonObject bodyJson = gsonParser.fromJson(json, JsonObject.class);
    String currentValue = null;

    if (bodyJson.has("value")) {
      JsonObject valueJson = bodyJson.getAsJsonObject("value");
      if (valueJson.has("fields")) {
        JsonObject fields = valueJson.getAsJsonObject("fields");
        if (fields.has("original")) {
          JsonObject originalValue = fields.getAsJsonObject("original");
          if (originalValue.has("stringValue")) {
            currentValue = originalValue.get("stringValue").getAsString();
          }
        }
      }
    }

    if (currentValue == null) {
      throw new RuntimeException("Missing JSON value: value.fields.original.stringValue");
    }

    // Convert recently-written value to ALL CAPS
    String newValue = currentValue.toUpperCase();

    // Update Firestore DB with ALL CAPS value
    Map<String, String> newFields = new HashMap<>();
    newFields.put("original", newValue);

    String[] pathComponents = context.resource().split("/documents/");
    if (pathComponents.length < 2) {
      throw new RuntimeException("Invalid Firestore doc path.");
    }
    String docPath = pathComponents[1].replace("\"", "");

    LOGGER.info(String.format("Replacing value: %s --> %s", currentValue, newValue));
    try {
      firestore.document(docPath).set(newFields, SetOptions.merge()).get();
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}

// [END functions_firebase_reactive]
