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

// [START eventarc_generic_handler]
import java.util.Map;
import org.json.JSONObject;
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
      @RequestBody String body, @RequestHeader Map<String, String> headers) {
    System.out.println("Event received!");
    
    // Log headers
    System.out.println("HEADERS:");
    headers.forEach((k, v) -> {
      if (!k.equals("Authorization")) {
        System.out.printf("%s: %s\n", k, v);
      }
    });
    System.out.println("");

    // Log body
    System.out.println("BODY:");
    JSONObject jsonBody = new JSONObject(body);
    String jsonString = jsonBody.toString(2);
    System.out.println(jsonString);

    // Return headers and body
    JSONObject json = new JSONObject();
    JSONObject jsonHeaders = new JSONObject();
    json.put("headers", jsonHeaders);
    json.put("body", jsonBody);

    return new ResponseEntity<String>(json.toString(), HttpStatus.OK);
  }
}
// [END eventarc_generic_handler]
