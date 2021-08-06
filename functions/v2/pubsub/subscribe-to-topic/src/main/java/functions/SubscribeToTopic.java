/*
 * Copyright 2021 Google LLC
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

// [START functions_pubsub_subscribe]

import com.google.events.cloud.functions.CloudEventsFunction;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.cloudevents.CloudEvent;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;

public class SubscribeToTopic implements CloudEventsFunction {
  private static final Logger logger = Logger.getLogger(SubscribeToTopic.class.getName());

  @Override
  public void accept(CloudEvent event) {
    if (event.getData() == null) {
      logger.info("No message provided");
      return;
    }

    Gson gson = new Gson();
    JsonObject eventDataJson = gson.fromJson(new String(event.getData().toBytes()),
      JsonObject.class);
    JsonObject messageJson = gson.fromJson(eventDataJson.get("message").toString(), JsonObject.class);
    String messageString = new String(Base64.getDecoder().decode(messageJson.get("data").getAsString()),
      StandardCharsets.UTF_8);

    logger.info(messageString);
  }
}
// [END functions_pubsub_subscribe]
