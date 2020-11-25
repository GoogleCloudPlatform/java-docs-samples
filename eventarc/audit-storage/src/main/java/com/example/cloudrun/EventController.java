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

// [START eventarc_gcs_handler]
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventController {

  private static final List<String> requiredFields =
      Arrays.asList("ce-id", "ce-source", "ce-type", "ce-specversion");

  @RequestMapping(value = "/", method = RequestMethod.POST)
  public ResponseEntity<String> receiveMessage(
      @RequestBody Map<String, Object> body, @RequestHeader Map<String, String> headers) {
    for (String field : requiredFields) {
      if (headers.get(field) == null) {
        String msg = String.format("Missing expected header: %s.", field);
        System.out.println(msg);
        return new ResponseEntity<String>(msg, HttpStatus.BAD_REQUEST);
      }
    }

    if (headers.get("ce-subject") == null) {
      String msg = "Missing expected header: ce-subject.";
      System.out.println(msg);
      return new ResponseEntity<String>(msg, HttpStatus.BAD_REQUEST);
    }

    String ceSubject = headers.get("ce-subject");
    String msg = "Detected change in GCS bucket: " + ceSubject;
    System.out.println(msg);
    return new ResponseEntity<String>(msg, HttpStatus.OK);
  }
}
// [END eventarc_gcs_handler]
