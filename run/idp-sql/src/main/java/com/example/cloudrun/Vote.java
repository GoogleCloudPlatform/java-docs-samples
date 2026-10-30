/*
 * Copyright 2021 Google LLC
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

import com.google.gson.JsonObject;
import java.sql.Timestamp;
import java.util.Objects;

public class Vote {
  private String uid;
  private String candidate;
  private Timestamp timeCast;

  public Vote(String uid, String candidate, Timestamp timeCast) {
    this.uid = Objects.requireNonNull(uid);
    this.candidate = Objects.requireNonNull(candidate);
    this.timeCast = Objects.requireNonNull(timeCast);
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = Objects.requireNonNull(uid);
  }

  public String getCandidate() {
    return candidate;
  }

  public void setCandidate(String candidate) {
    this.candidate = Objects.requireNonNull(candidate);
  }

  public Timestamp getTimeCast() {
    return timeCast;
  }

  public void setTimeCast(Timestamp timeCast) {
    this.timeCast = Objects.requireNonNull(timeCast);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Vote vote = (Vote) o;
    return Objects.equals(uid, vote.uid)
        && Objects.equals(candidate, vote.candidate)
        && Objects.equals(timeCast, vote.timeCast);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uid, candidate, timeCast);
  }

  @Override
  public String toString() {
    JsonObject vote = new JsonObject();
    vote.addProperty("uid", this.uid);
    vote.addProperty("candidate", this.candidate);
    vote.addProperty("timestamp", this.timeCast.toString());
    return vote.toString();
  }
}
