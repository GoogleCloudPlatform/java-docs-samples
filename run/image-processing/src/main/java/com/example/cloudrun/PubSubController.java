/*
 * Copyright 2019 Google LLC
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

// [START cloudrun_imageproc_controller]
// [START run_imageproc_controller]
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

// PubsubController consumes a Pub/Sub message.
@RestController
public class PubSubController {
  @RequestMapping(value = "/", method = RequestMethod.POST)
  public ResponseEntity receiveMessage(@RequestBody Body body) {
    // Get PubSub message from request body.
    Body.Message message = body.getMessage();
    if (message == null) {
      String msg = "Bad Request: invalid Pub/Sub message format";
      System.out.println(msg);
      return new ResponseEntity(msg, HttpStatus.BAD_REQUEST);
    }

    // Decode the Pub/Sub message.
    String pubSubMessage = message.getData();
    JsonObject data;
    try {
      String decodedMessage = new String(Base64.getDecoder().decode(pubSubMessage));
      data = new JsonParser().parse(decodedMessage).getAsJsonObject();
    } catch (Exception e) {
      String msg = "Error: Invalid Pub/Sub message: data property is not valid base64 encoded JSON";
      System.out.println(msg);
      return new ResponseEntity(msg, HttpStatus.BAD_REQUEST);
    }

    // Validate the message is a Cloud Storage event.
    if (data.get("name") == null || data.get("bucket") == null) {
      String msg = "Error: Invalid Cloud Storage notification: expected name and bucket properties";
      System.out.println(msg);
      return new ResponseEntity(msg, HttpStatus.BAD_REQUEST);
    }

    try {
      ImageMagick.blurOffensiveImages(data);
    } catch (Exception e) {
      String msg = String.format("Error: Blurring image: %s", e.getMessage());
      System.out.println(msg);
      return new ResponseEntity(msg, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return new ResponseEntity(HttpStatus.OK);
  }
}
// [END run_imageproc_controller]
// [END cloudrun_imageproc_controller]
