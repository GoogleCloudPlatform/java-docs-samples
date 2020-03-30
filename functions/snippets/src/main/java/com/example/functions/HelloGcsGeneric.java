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

// [START functions_helloworld_storage_generic]
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import java.util.logging.Logger;

public class HelloGcsGeneric implements BackgroundFunction<GcsEvent> {
  private static final Logger LOGGER = Logger.getLogger(HelloGcs.class.getName());

  @Override
  public void accept(GcsEvent event, Context context) {
    LOGGER.info("Event: " + context.eventId());
    LOGGER.info("Event Type: " + context.eventType());
    LOGGER.info("Bucket: " + event.getBucket());
    LOGGER.info("File: " + event.getName());
    LOGGER.info("Metageneration: " + event.getMetageneration());
    LOGGER.info("Created: " + event.getTimeCreated());
    LOGGER.info("Updated: " + event.getUpdated());
  }
}
// [END functions_helloworld_storage_generic]
