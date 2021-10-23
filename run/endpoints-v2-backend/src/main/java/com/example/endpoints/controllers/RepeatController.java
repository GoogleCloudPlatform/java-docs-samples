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

package com.example.endpoints.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/api/v1")
@RestController
public class RepeatController {

  @GetMapping("/repeat")
  public ResponseEntity<String> repeat(@RequestParam("text") String text,
                                       @RequestParam("times") Integer times) {
    StringBuilder response = new StringBuilder();
    for (int i = 0; i < times - 1; i++) {
      response.append(text).append(", ");
    }
    response.append(text).append("!");

    return new ResponseEntity<>(response.toString(), HttpStatus.OK);
  }
}
