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

package functions;

// [START functions_firebase_reactive]

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.functions.Context;
import com.google.cloud.functions.RawBackgroundFunction;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FirebaseFirestoreReactive implements RawBackgroundFunction {

  // Use GSON (https://github.com/google/gson) to parse JSON content.
  private static final Gson gson = new Gson();

  private static final Logger logger = Logger.getLogger(FirebaseFirestoreReactive.class.getName());
  private static final Firestore FIRESTORE = FirestoreOptions.getDefaultInstance().getService();

  private final Firestore firestore;

  public FirebaseFirestoreReactive() {
    this(FIRESTORE);
  }

  FirebaseFirestoreReactive(Firestore firestore) {
    this.firestore = firestore;
  }

  @Override
  public void accept(String json, Context context) {
    // Get the recently-written value
    JsonObject body = gson.fromJson(json, JsonObject.class);
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
      throw new IllegalArgumentException("Malformed JSON: " + json);
    }

    // Convert recently-written value to ALL CAPS
    String newValue = currentValue.toUpperCase(Locale.getDefault());

    // Update Firestore DB with ALL CAPS value
    Map<String, String> newFields = Map.of("original", newValue);

    String affectedDoc = context.resource().split("/documents/")[1].replace("\"", "");

    if (!currentValue.equals(newValue)) {
      // The stored value needs to be updated
      // Write the upper-cased value to Firestore
      logger.info(String.format("Replacing value: %s --> %s", currentValue, newValue));
      try {
        FIRESTORE.document(affectedDoc).set(newFields, SetOptions.merge()).get();
      } catch (ExecutionException | InterruptedException e) {
        logger.log(Level.SEVERE, "Error updating Firestore document: " + e.getMessage(), e);
      }
    } else {
      // The stored value is already upper-case, and doesn't need updating.
      // (Don't perform a "second" write, since that could trigger an infinite loop.)
      logger.info(String.format("Value is already upper-case."));
    }
  }
}

// [END functions_firebase_reactive]
