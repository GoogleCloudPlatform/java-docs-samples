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
// [START functions_tips_retry]
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.text.ParseException;
import java.util.Map;

public class PubSub {
  // Use Gson (https://github.com/google/gson) to parse JSON content.
  private Gson gsonParser = new Gson();

  // [END functions_tips_infinite_retries]
  // [START functions_tips_retry]
  public String retryPubSub(PubSubMessage message) throws Exception, ParseException {
    JsonObject body = gsonParser.fromJson(message.data, JsonObject.class);
    
    boolean retry = body.has("retry") && body.get("retry").getAsBoolean();
    if (body.has("retry")) {
      retry = body.get("retry").getAsBoolean();
    }
    
    if (retry) {
      throw new RuntimeException("Retrying...");
    } else {
      throw new RuntimeException("Not retrying...");
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

    LocalDateTime timestamp = LocalDateTime.now();
    if (body.has("timestamp")) {
      timestamp = LocalDateTime.parse(body.get("timestamp").getAsString());
    }
    long eventAge = Duration.between(timestamp, LocalDateTime.now()).toMillis();

    // Ignore events that are too old
    if (eventAge > MAX_AGE) {
      System.out.printf("Dropping event %s.", body);
      return;
    }

    // Do what the function is supposed to do
    System.out.printf("Processing event %s.", body);
  }

  // [START functions_tips_retry]
  // Cloud Functions will marshall it using Gson.
  public class PubSubMessage {
    String data;
    Map<String, String> attributes;
    String messageId;
    String publishTime;
  }
}
// [END functions_tips_retry]
// [END functions_tips_infinite_retries]
