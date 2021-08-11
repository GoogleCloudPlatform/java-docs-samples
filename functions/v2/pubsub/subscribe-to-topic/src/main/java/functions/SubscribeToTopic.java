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

package functions.v2;

// [START functions_cloudevent_pubsub]

import com.example.cloud.functions.v2.eventpojos.PubSubBody;
import com.google.cloud.functions.CloudEventsFunction;
import com.google.gson.Gson;
import io.cloudevents.CloudEvent;
import java.util.Base64;
import java.util.logging.Logger;

public class SubscribeToTopic implements CloudEventsFunction {
  private static final Logger logger = Logger.getLogger(SubscribeToTopic.class.getName());

  @Override
  public void accept(CloudEvent event) {

    Gson gson = new Gson();
    PubSubBody pubSubBody = gson.fromJson(new String(event.getData().toBytes()),
      PubSubBody.class);
    String messageString = new String(Base64.getDecoder().decode(pubSubBody.getMessage().getData()));

    logger.info(messageString);
  }
}
// [END functions_cloudevent_pubsub]
