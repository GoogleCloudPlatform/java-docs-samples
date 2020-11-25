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

import java.time.Instant;
import org.springframework.data.annotation.Id;

public class Vote {

  @Id
  private Integer voteId;

  private Instant timeCast;

  private String candidate;

  public Vote(String candidate) {
    this.timeCast = Instant.now();
    this.candidate = candidate;
  }

  public Integer getVoteId() {
    return voteId;
  }

  public void setVoteId(Integer voteId) {
    this.voteId = voteId;
  }

  public Instant getTimeCast() {
    return timeCast;
  }

  public void setTimeCast(Instant timeCast) {
    this.timeCast = timeCast;
  }

  public String getCandidate() {
    return candidate;
  }

  public void setCandidate(String candidate) {
    this.candidate = candidate;
  }
}
