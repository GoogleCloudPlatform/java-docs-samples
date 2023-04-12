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

// [START functions_cloudevent_firebase_rtdb]
import com.google.cloud.functions.CloudEventsFunction;
import com.google.events.firebase.database.v1.ReferenceEventData;
import com.google.protobuf.util.JsonFormat;
import io.cloudevents.CloudEvent;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class FirebaseRtdb implements CloudEventsFunction {
  private static final Logger logger = Logger.getLogger(FirebaseRtdb.class.getName());

  @Override
  public void accept(CloudEvent event) throws Exception {
    if (event.getData() == null) {
      logger.info("No data found in event");
      return;
    }

    ReferenceEventData.Builder builder = ReferenceEventData.newBuilder();
    JsonFormat.Parser jsonParser = JsonFormat.parser().ignoringUnknownFields();
    jsonParser.merge(new String(event.getData().toBytes(), StandardCharsets.UTF_8), builder);
    ReferenceEventData data = builder.build();

    logger.info("Function triggered by change to: " + event.getSource().toString());

    if (data.hasDelta()) {
      logger.info("Delta: " + data.getDelta().toString());
    }

    if (data.hasData()) {
      logger.info("Data: " + data.getData().toString());
    }
  }
}

// [END functions_cloudevent_firebase_rtdb]
