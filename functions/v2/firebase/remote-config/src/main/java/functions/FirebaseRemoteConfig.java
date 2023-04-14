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

// [START functions_cloudevent_firebase_remote_config]
import com.google.cloud.functions.CloudEventsFunction;
import com.google.events.firebase.remoteconfig.v1.RemoteConfigEventData;
import com.google.protobuf.util.JsonFormat;
import io.cloudevents.CloudEvent;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class FirebaseRemoteConfig implements CloudEventsFunction {
  private static final Logger logger = Logger.getLogger(FirebaseRemoteConfig.class.getName());

  @Override
  public void accept(CloudEvent event) throws Exception {
    if (event.getData() == null) {
      logger.info("No data found in event");
      return;
    }

    RemoteConfigEventData.Builder builder = RemoteConfigEventData.newBuilder();
    JsonFormat.Parser jsonParser = JsonFormat.parser().ignoringUnknownFields();
    jsonParser.merge(new String(event.getData().toBytes(), StandardCharsets.UTF_8), builder);
    RemoteConfigEventData data = builder.build();

    logger.info("Update type: " + data.getUpdateType().name());
    logger.info("Origin: " + data.getUpdateOrigin().name());
    logger.info("Version: " + data.getVersionNumber());
  }
}

// [END functions_cloudevent_firebase_remote_config]
