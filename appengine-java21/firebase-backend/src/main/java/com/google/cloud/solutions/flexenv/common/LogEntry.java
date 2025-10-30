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

package com.google.cloud.solutions.flexenv.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.firebase.database.ServerValue;
import java.util.Map;

/*
 * An instance of LogEntry represents a user event log, such as signin/out and switching a channel.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogEntry {
  private String tag;
  private String log;
  private Long time;

  public LogEntry() {}

  public LogEntry(String tag, String log) {
    this.tag = tag;
    this.log = log;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getLog() {
    return log;
  }

  public void setLog(String log) {
    this.log = log;
  }

  public Map<String, String> getTime() {
    return ServerValue.TIMESTAMP;
  }

  public void setTime(Long time) {
    this.time = time;
  }

  @JsonIgnore
  public Long getTimeLong() {
    return time;
  }
}
