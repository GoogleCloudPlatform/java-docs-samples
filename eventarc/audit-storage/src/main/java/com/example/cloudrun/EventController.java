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

// [START eventarc_audit_storage_handler]
// [START eventarc_http_quickstart_handler]
import io.cloudevents.CloudEvent;
import io.cloudevents.rw.CloudEventRWException;
import io.cloudevents.spring.http.CloudEventHttpUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventController {

  @RequestMapping(value = "/", method = RequestMethod.POST, consumes = "application/json")
  public ResponseEntity<String> receiveMessage(
      @RequestBody String body, @RequestHeader HttpHeaders headers) {
    CloudEvent event;
    try {
      event =
          CloudEventHttpUtils.fromHttp(headers)
              .withData(headers.getContentType().toString(), body.getBytes())
              .build();
    } catch (CloudEventRWException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    String ceSubject = event.getSubject();
    String msg = "Detected change in Cloud Storage bucket: " + ceSubject;
    System.out.println(msg);
    return new ResponseEntity<>(msg, HttpStatus.OK);
  }
}
// [END eventarc_audit_storage_handler]
// [END eventarc_http_quickstart_handler]
