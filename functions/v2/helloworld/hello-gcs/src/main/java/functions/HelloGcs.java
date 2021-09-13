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

// [START functions_cloudevent_storage]
import com.google.cloud.functions.CloudEventsFunction;
import com.google.gson.Gson;
import functions.eventpojos.GcsEvent;
import io.cloudevents.CloudEvent;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class HelloGcs implements CloudEventsFunction {
  private static final Logger logger = Logger.getLogger(HelloGcs.class.getName());

  @Override
  public void accept(CloudEvent event) {
    logger.info("Event: " + event.getId());
    logger.info("Event Type: " + event.getType());

    if (event.getData() != null) {
      String cloudEventData = new String(event.getData().toBytes(), StandardCharsets.UTF_8);
      Gson gson = new Gson();
      GcsEvent gcsEvent = gson.fromJson(cloudEventData, GcsEvent.class);

      logger.info("Bucket: " + gcsEvent.getBucket());
      logger.info("File: " + gcsEvent.getName());
      logger.info("Metageneration: " + gcsEvent.getMetageneration());
      logger.info("Created: " + gcsEvent.getTimeCreated());
      logger.info("Updated: " + gcsEvent.getUpdated());
    }
  }
}

// [END functions_cloudevent_storage]
