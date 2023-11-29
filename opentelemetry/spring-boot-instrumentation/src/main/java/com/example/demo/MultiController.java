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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class MultiController {
  Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired private WebClient client;

  /** handleMulti handles an http request by making 3-7 http requests to the /single endpoint. */
  // [START opentelemetry_instrumentation_handle_multi]
  @GetMapping("/multi")
  public Mono<String> index() throws Exception {
    int subRequests = ThreadLocalRandom.current().nextInt(3, 8);

    // Write a structured log with the request context, which allows the log to
    // be linked with the trace for this request.
    logger.info("handle /multi request with subRequests={}", subRequests);

    // Make 3-7 http requests to the /single endpoint.
    return Flux.range(0, subRequests)
        .concatMap(
            i -> client.get().uri("http://localhost:8080/single").retrieve().bodyToMono(Void.class))
        .then(Mono.just("ok"));
  }
  // [END opentelemetry_instrumentation_handle_multi]
}
