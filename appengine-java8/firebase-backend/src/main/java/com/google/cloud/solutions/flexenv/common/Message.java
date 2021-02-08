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
 * An instance of Message represents an actual message pushed to a channel.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {
  private String text;
  private String displayName;
  private Long time;

  public Message() {}

  public Message(String text, String displayName) {
    this.text = text;
    this.displayName = displayName;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
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
