/*
 * Copyright 2016 Google Inc. All Rights Reserved.
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

package com.example.appengine;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.User;
import com.google.auto.value.AutoValue;
import org.joda.time.Instant;

import java.util.Date;

import javax.annotation.Nullable;

@AutoValue
public abstract class Greeting {
  static Greeting create(Entity entity) {
    User user = (User) entity.getProperty("user");
    Instant date = new Instant((Date) entity.getProperty("date"));
    String content  = (String) entity.getProperty("content");
    return new AutoValue_Greeting(user, date, content);
  }

  @Nullable
  public abstract User getUser();

  public abstract Instant getDate();

  public abstract String getContent();
}
