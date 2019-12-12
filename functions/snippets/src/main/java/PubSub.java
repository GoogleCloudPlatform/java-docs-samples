/*
 * Copyright 2019 Google LLC
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

// [START functions_tips_infinite_retries]
import java.sql.Timestamp;
// [START functions_tips_retry]
import java.text.ParseException;
import java.util.Map;


import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class PubSub {
  // Use GSON (https://github.com/google/gson) to parse JSON content.
  private Gson gsonParser = new Gson();

  // [END functions_tips_infinite_retries]
  // [START functions_tips_retry]
  public String retryPubSub(PubSubMessage message) throws Exception, ParseException {
    JsonObject body = gsonParser.fromJson(message.data, JsonObject.class);
    
    boolean retry = false;
    if (body.has("retry")) {
      retry = body.get("retry").getAsBoolean();
    }
    
    if (retry) {
      throw new Exception("Retrying...");
    } else {
      throw new Exception("Not retrying...");
    }
  }
  // [END functions_tips_retry]

  // [START functions_tips_infinite_retries]
  /**
   * Background Cloud Function that only executes within
   * a certain time period after the triggering event
   */
  public void infiniteRetryPubSub(PubSubMessage message) throws Exception, ParseException {
    JsonObject body = gsonParser.fromJson(message.data, JsonObject.class);
    final long MAX_AGE = 10_000;

    long eventAge = 0;
    if (body.has("timestamp")) {
      long ts = Timestamp.valueOf(body.get("timestamp").getAsString()).getTime();
      eventAge = System.currentTimeMillis() - ts;
    }

    // Ignore events that are too old
    if (eventAge > MAX_AGE) {
      System.out.printf("Dropping event %s with age %ld ms.", body, eventAge);
      return;
    }

    // Do what the function is supposed to do
    System.out.printf("Processing event %s with age %ld ms.", body, eventAge);
  }

  // [START functions_tips_retry]
  public class PubSubMessage {
    String data;
    Map<String, String> attributes;
    String messageId;
    String publishTime;
  }
}
// [END functions_tips_retry]
// [END functions_tips_infinite_retries]