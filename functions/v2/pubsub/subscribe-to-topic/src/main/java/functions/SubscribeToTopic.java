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

// [START functions_cloudevent_pubsub]
import static io.cloudevents.core.CloudEventUtils.mapData;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.functions.CloudEventsFunction;
import functions.eventpojos.PubSubBody;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.jackson.PojoCloudEventDataMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;

public class SubscribeToTopic implements CloudEventsFunction {
  private static final Logger logger = Logger.getLogger(SubscribeToTopic.class.getName());

  @Override
  public void accept(CloudEvent event) {
    logger.info("Event: " + event.getId());
    logger.info("Event Type: " + event.getType());

    // Unmarshal Cloud Event data
    ObjectMapper objectMapper =
        new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    PojoCloudEventData<PubSubBody> cloudEventData =
        mapData(event, PojoCloudEventDataMapper.from(objectMapper, PubSubBody.class));

    if (cloudEventData != null) {
      PubSubBody body = cloudEventData.getValue();
      // Retrieve PubSub message data
      String encodedData = body.getMessage().getData();
      String decodedData =
          new String(Base64.getDecoder().decode(encodedData), StandardCharsets.UTF_8);
      logger.info("Event data: " + decodedData);
    }
  }
}
// [END functions_cloudevent_pubsub]
