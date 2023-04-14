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
import com.google.events.cloud.storage.v1.StorageObjectData;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.cloudevents.CloudEvent;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class HelloGcs implements CloudEventsFunction {
  private static final Logger logger = Logger.getLogger(HelloGcs.class.getName());

  @Override
  public void accept(CloudEvent event) throws InvalidProtocolBufferException {
    logger.info("Event: " + event.getId());
    logger.info("Event Type: " + event.getType());

    if (event.getData() == null) {
      logger.warning("No data found in cloud event payload!");
      return;
    }

    String cloudEventData = new String(event.getData().toBytes(), StandardCharsets.UTF_8);
    StorageObjectData.Builder builder = StorageObjectData.newBuilder();
    JsonFormat.parser().merge(cloudEventData, builder);
    StorageObjectData data = builder.build();

    logger.info("Bucket: " + data.getBucket());
    logger.info("File: " + data.getName());
    logger.info("Metageneration: " + data.getMetageneration());
    logger.info("Created: " + data.getTimeCreated());
    logger.info("Updated: " + data.getUpdated());
  }
}

// [END functions_cloudevent_storage]
