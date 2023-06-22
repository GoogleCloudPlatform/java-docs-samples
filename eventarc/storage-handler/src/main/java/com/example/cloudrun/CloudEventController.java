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

package com.example.cloudrun;

// [START eventarc_storage_cloudevent_handler]
import com.google.events.cloud.storage.v1.StorageObjectData;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.JsonFormat;
import io.cloudevents.CloudEvent;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CloudEventController {

  @RequestMapping(value = "/", method = RequestMethod.POST, consumes = "application/json")
  ResponseEntity<String> handleCloudEvent(@RequestBody CloudEvent cloudEvent)
      throws InvalidProtocolBufferException {

    // CloudEvent information
    System.out.println("Id: " + cloudEvent.getId());
    System.out.println("Source: " + cloudEvent.getSource());
    System.out.println("Type: " + cloudEvent.getType());

    String json = new String(cloudEvent.getData().toBytes());
    StorageObjectData.Builder builder = StorageObjectData.newBuilder();
    JsonFormat.parser().merge(json, builder);
    StorageObjectData data = builder.build();

    // Convert protobuf timestamp to java Instant
    Timestamp ts = data.getUpdated();
    Instant updated = Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
    String msg =
        String.format(
            "Cloud Storage object changed: %s/%s modified at %s\n",
            data.getBucket(), data.getName(), updated);

    System.out.println(msg);
    return ResponseEntity.ok().body(msg);
  }

  // Handle exceptions from CloudEvent Message Converter
  @ExceptionHandler(IllegalStateException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid CloudEvent received")
  public void noop() {
    return;
  }
}
// [END eventarc_storage_cloudevent_handler]
