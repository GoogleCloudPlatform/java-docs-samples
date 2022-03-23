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

// [START functions_log_cloudevent]
import com.google.cloud.functions.CloudEventsFunction;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.cloudevents.CloudEvent;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class LogCloudEvent implements CloudEventsFunction {
  private static final Logger logger = Logger.getLogger(LogCloudEvent.class.getName());

  @Override
  public void accept(CloudEvent event) {
    // Print out details from the CloudEvent
    // The type of event related to the originating occurrence
    logger.info("Event Type: " + event.getType());
    // The subject of the event in the context of the event producer
    logger.info("Event Subject: " + event.getSubject());

    if (event.getData() != null) {
      // Extract data from CloudEvent wrapper
      String cloudEventData = new String(event.getData().toBytes(), StandardCharsets.UTF_8);

      Gson gson = new Gson();
      // Convert data into a JSON object
      JsonObject eventData = gson.fromJson(cloudEventData, JsonObject.class);

      // Extract Cloud Audit Log data from protoPayload
      // https://cloud.google.com/logging/docs/audit#audit_log_entry_structure
      JsonObject payload = eventData.getAsJsonObject("protoPayload");
      logger.info("API Method: " + payload.get("methodName").getAsString());
      logger.info("Resource name: " + payload.get("resourceName").getAsString());

      JsonObject auth = payload.getAsJsonObject("authenticationInfo");
      if (auth != null) {
        // The email address of the authenticated user 
        // (or service account on behalf of third party principal) making the request
        logger.info("Authenticated User: " + auth.get("principalEmail").getAsString()); 
      }
    }
  }
}
// [END functions_log_cloudevent]
