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

// [START functions_helloworld_storage]
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import functions.eventpojos.GcsEvent;
import java.util.logging.Logger;

/**
 * Example Cloud Storage-triggered function.
 * This function can process any event from Cloud Storage.
 */
public class HelloGcs implements BackgroundFunction<GcsEvent> {
  private static final Logger logger = Logger.getLogger(HelloGcs.class.getName());

  @Override
  public void accept(GcsEvent event, Context context) {
    logger.info("Event: " + context.eventId());
    logger.info("Event Type: " + context.eventType());
    logger.info("Bucket: " + event.getBucket());
    logger.info("File: " + event.getName());
    logger.info("Metageneration: " + event.getMetageneration());
    logger.info("Created: " + event.getTimeCreated());
    logger.info("Updated: " + event.getUpdated());
  }
}

// [END functions_helloworld_storage]
