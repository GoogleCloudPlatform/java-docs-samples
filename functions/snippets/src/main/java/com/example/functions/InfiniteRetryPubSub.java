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

// [START functions_tips_infinite_retries]
import com.google.api.client.util.DateTime;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InfiniteRetryPubSub implements BackgroundFunction<PubSubMessage> {
  private static final Logger LOGGER = Logger.getLogger(InfiniteRetryPubSub.class.getName());
  private static final long MAX_EVENT_AGE = 10_000;

  // Use Gson (https://github.com/google/gson) to parse JSON content.
  private Gson gsonParser = new Gson();

  /**
   * Background Cloud Function that only executes within
   * a certain time period after the triggering event
   */
  @Override
  public void accept(com.example.functions.PubSubMessage message, Context context) {
    ZonedDateTime utcNow = ZonedDateTime.now(ZoneOffset.UTC);
    ZonedDateTime timestamp = utcNow;

    JsonObject body = gsonParser.fromJson(message.data, JsonObject.class);
    if (body != null && body.has("timestamp")) {
      String tz = body.get("timestamp").getAsString();
      timestamp = ZonedDateTime.parse(tz);
    }
    long eventAge = Duration.between(timestamp, utcNow).toMillis();

    // Ignore events that are too old
    if (eventAge > MAX_EVENT_AGE) {
      LOGGER.info(String.format("Dropping event %s.", body));
      return;
    }

    // Do what the function is supposed to do
    LOGGER.info(String.format("Processing event %s.", body));
  }
}
// [END functions_tips_infinite_retries]
