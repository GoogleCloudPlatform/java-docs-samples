/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.demo;

import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SingleController {
  Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * handleSingle handles an http request by sleeping for 100-200 ms. It writes the number of
   * milliseconds slept as its response.
   *
   * @throws InterruptedException
   */
  // [START opentelemetry_instrumentation_handle_single]
  @GetMapping("/single")
  public String index() throws InterruptedException {
    int sleepMillis = ThreadLocalRandom.current().nextInt(100, 200);
    logger.info("Going to sleep for {}", sleepMillis);
    Thread.sleep(sleepMillis);
    logger.info("Finishing the request");
    return String.format("slept %s\n", sleepMillis);
  }
  // [END opentelemetry_instrumentation_handle_single]
}
