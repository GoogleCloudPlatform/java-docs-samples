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

package com.example.functions.pubsub;

// [START functions_pubsub_subscribe]
import com.example.functions.pubsub.eventpojos.PubSubMessage;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;

public class SubscribeToTopic implements BackgroundFunction<PubSubMessage> {
  private static final Logger LOGGER = Logger.getLogger(SubscribeToTopic.class.getName());

  @Override
  public void accept(PubSubMessage message, Context context) {
    if (message.getData() == null) {
      LOGGER.info("No message provided");
      return;
    }

    String messageString = new String(
        Base64.getDecoder().decode(message.getData().getBytes(StandardCharsets.UTF_8)),
        StandardCharsets.UTF_8);
    LOGGER.info(messageString);
  }
}
// [END functions_pubsub_subscribe]
