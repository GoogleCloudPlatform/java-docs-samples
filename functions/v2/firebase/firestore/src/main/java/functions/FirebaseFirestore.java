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

// [START functions_cloudevent_firebase_firestore]
import com.google.cloud.functions.CloudEventsFunction;
import com.google.events.cloud.firestore.v1.DocumentEventData;
import com.google.protobuf.InvalidProtocolBufferException;
import io.cloudevents.CloudEvent;
import java.util.logging.Logger;

public class FirebaseFirestore implements CloudEventsFunction {
  private static final Logger logger = Logger.getLogger(FirebaseFirestore.class.getName());

  @Override
  public void accept(CloudEvent event) throws InvalidProtocolBufferException {
    DocumentEventData firestoreEventData = DocumentEventData
        .parseFrom(event.getData().toBytes());

    logger.info("Function triggered by event on: " + event.getSource());
    logger.info("Event type: " + event.getType());

    logger.info("Old value:");
    logger.info(firestoreEventData.getOldValue().toString());

    logger.info("New value:");
    logger.info(firestoreEventData.getValue().toString());
  }
}

// [END functions_cloudevent_firebase_firestore]
