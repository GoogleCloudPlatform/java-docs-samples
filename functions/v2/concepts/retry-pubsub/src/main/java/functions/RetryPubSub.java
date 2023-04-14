/*
 * Copyright 2023 Google LLC
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

// [START functions_cloudevent_tips_retry]

import com.google.cloud.functions.CloudEventsFunction;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import functions.eventpojos.PubSubBody;
import io.cloudevents.CloudEvent;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;

public class RetryPubSub implements CloudEventsFunction {
  private static final Logger logger = Logger.getLogger(RetryPubSub.class.getName());

  // Use Gson (https://github.com/google/gson) to parse JSON content.
  private static final Gson gson = new Gson();

  @Override
  public void accept(CloudEvent event) throws Exception {
    if (event.getData() == null) {
      logger.warning("No data found in event!");
      return;
    }

    // Extract Cloud Event data and convert to PubSubBody
    String cloudEventData = new String(event.getData().toBytes(), StandardCharsets.UTF_8);
    PubSubBody body = gson.fromJson(cloudEventData, PubSubBody.class);

    String encodedData = body.getMessage().getData();
    String decodedData =
        new String(Base64.getDecoder().decode(encodedData), StandardCharsets.UTF_8);

    // Retrieve and decode PubSubMessage data into a JsonElement.
    // Function is expecting a user-supplied JSON message which determines whether
    // to retry or not.
    JsonElement jsonPubSubMessageElement = gson.fromJson(decodedData, JsonElement.class);

    boolean retry = false;
    // Get the value of the "retry" JSON parameter, if one exists
    if (jsonPubSubMessageElement != null && jsonPubSubMessageElement.isJsonObject()) {
      JsonObject jsonPubSubMessageObject = jsonPubSubMessageElement.getAsJsonObject();

      if (jsonPubSubMessageObject.has("retry")
          && jsonPubSubMessageObject.get("retry").getAsBoolean()) {
        retry = true;
      }
    }

    // Retry if appropriate
    if (retry) {
      // Throwing an exception causes the execution to be retried
      throw new RuntimeException("Retrying...");
    } else {
      logger.info("Not retrying...");
    }
  }
}
// [END functions_cloudevent_tips_retry]
