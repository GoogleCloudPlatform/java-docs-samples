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

// [START functions_helloworld_storage]
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;

import java.util.logging.Logger;

public class HelloGcs implements BackgroundFunction<GcsEvent> {
  private static final Logger LOGGER = Logger.getLogger(HelloGcs.class.getName());

  @Override
  public void accept(GcsEvent event, Context context) {
    if ("google.storage.object.finalize".equals(context.eventType())) {
      // Default event type for GCS-triggered functions
      LOGGER.info(String.format("File %s uploaded.", event.name));
    } else {
      LOGGER.warning(String.format("Unsupported event type: %s", context.eventType()));
    }
  }
}
// [END functions_helloworld_storage]