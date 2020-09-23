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

package com.example.cloudsql.r2dbcsample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class VoteController {

  @Autowired
  private VoteRepository voteRepository;

  @PostMapping("/vote")
  public Mono<String> vote(ServerWebExchange serverWebExchange) {
    return serverWebExchange.getFormData()
        .flatMap(formData -> voteRepository.save(new Vote(formData.getFirst("team"))))
        .map(vote ->
            String.format("Vote successfully cast for '%s' at time %s!%n",
                vote.getCandidate(), vote.getTimeCast()));
  }
}
