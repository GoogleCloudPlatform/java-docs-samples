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

package com.example.cloudrun;

// [START eventarc_pubsub_handler]
import com.example.cloudrun.eventpojos.PubSubBody;
import java.util.Base64;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventController {
  @RequestMapping(value = "/", method = RequestMethod.POST)
  public ResponseEntity<String> receiveMessage(
      @RequestBody PubSubBody body, @RequestHeader Map<String, String> headers) {
    // Get PubSub message from request body.
    PubSubBody.PubSubMessage message = body.getMessage();
    if (message == null) {
      String msg = "No Pub/Sub message received.";
      System.out.println(msg);
      return new ResponseEntity<String>(msg, HttpStatus.BAD_REQUEST);
    }

    String data = message.getData();
    if (data == null || data.isEmpty()) {
      String msg = "Invalid Pub/Sub message format.";
      System.out.println(msg);
      return new ResponseEntity<String>(msg, HttpStatus.BAD_REQUEST);
    }

    String name =
        !StringUtils.isEmpty(data) ? new String(Base64.getDecoder().decode(data)) : "World";
    String ceId = headers.getOrDefault("ce-id", "");
    String msg = String.format("Hello, %s! ID: %s", name, ceId);
    System.out.println(msg);
    return new ResponseEntity<String>(msg, HttpStatus.OK);
  }
}
// [END eventarc_pubsub_handler]
