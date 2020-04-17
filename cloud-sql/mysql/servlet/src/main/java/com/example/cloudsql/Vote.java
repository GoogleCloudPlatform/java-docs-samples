/*
 * Copyright 2018 Google LLC
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

package com.example.cloudsql;

import java.sql.Timestamp;
import java.util.Locale;

public class Vote {

  private String candidate;
  private Timestamp timeCast;

  public Vote(String candidate, Timestamp timeCast) {
    this.candidate = candidate.toUpperCase(Locale.ENGLISH);
    this.timeCast = new Timestamp(timeCast.getTime());
  }

  public String getCandidate() {
    return candidate;
  }

  public void setCandidate(String candidate) {
    this.candidate = candidate.toUpperCase(Locale.ENGLISH);
  }

  public Timestamp getTimeCast() {
    return new Timestamp(timeCast.getTime());
  }

  public void setTimeCast(Timestamp timeCast) {
    this.timeCast = new Timestamp(timeCast.getTime());
  }

  public String toString() {
    return String.format("Vote(candidate=%s,timeCast=%s)", this.candidate, this.timeCast);
  }
}
