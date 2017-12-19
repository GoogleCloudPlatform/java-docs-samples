/*
 * Copyright 2016 Google Inc.
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

package com.example.gaefirebaseeventproxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.utils.SystemProperty;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class FirebaseEventProxy {

  private static final Logger log = Logger.getLogger(FirebaseEventProxy.class.getName());

  public FirebaseEventProxy() {
    String firebaseLocation = "https://crackling-torch-392.firebaseio.com";
    Map<String, Object> databaseAuthVariableOverride = new HashMap<String, Object>();
    // uid and provider will have to match what you have in your firebase security rules
    databaseAuthVariableOverride.put("uid", "gae-firebase-event-proxy");
    databaseAuthVariableOverride.put("provider", "com.example");
    try {
      FirebaseOptions options = new FirebaseOptions.Builder()
          .setServiceAccount(new FileInputStream("gae-firebase-secrets.json"))
          .setDatabaseUrl(firebaseLocation)
          .setDatabaseAuthVariableOverride(databaseAuthVariableOverride).build();
      FirebaseApp.initializeApp(options);
    } catch (IOException e) {
      throw new RuntimeException(
          "Error reading firebase secrets from file: src/main/webapp/gae-firebase-secrets.json: "
          + e.getMessage());
    }
  }

  public void start() {
    DatabaseReference firebase = FirebaseDatabase.getInstance().getReference();

    // Subscribe to value events. Depending on use case, you may want to subscribe to child events
    // through childEventListener.
    firebase.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot snapshot) {
        if (snapshot.exists()) {
          try {
            // Replace the URL with the url of your own listener app.
            URL dest = new URL("http://gae-firebase-listener-python.appspot.com/log");
            HttpURLConnection connection = (HttpURLConnection) dest.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // Rely on X-Appengine-Inbound-Appid to authenticate. Turning off redirects is
            // required to enable.
            connection.setInstanceFollowRedirects(false);

            // Fill out header if in dev environment
            if (SystemProperty.environment.value() != SystemProperty.Environment.Value.Production) {
              connection.setRequestProperty("X-Appengine-Inbound-Appid", "dev-instance");
            }

            // Put Firebase data into http request
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("&fbSnapshot=");
            // Convert value to JSON using Jackson
            String json = new ObjectMapper().writeValueAsString(snapshot.getValue(false));
            stringBuilder.append(URLEncoder.encode(json, "UTF-8"));
            connection.getOutputStream().write(stringBuilder.toString().getBytes());
            if (connection.getResponseCode() != 200) {
              log.severe("Forwarding failed");
            } else {
              log.info("Sent: " + json);
            }
          } catch (JsonProcessingException e) {
            log.severe("Unable to convert Firebase response to JSON: " + e.getMessage());
          } catch (IOException e) {
            log.severe("Error in connecting to app engine: " + e.getMessage());
          }
        }
      }

      @Override
      public void onCancelled(DatabaseError error) {
        log.severe("Firebase connection cancelled: " + error.getMessage());
      }
    });
  }
}
