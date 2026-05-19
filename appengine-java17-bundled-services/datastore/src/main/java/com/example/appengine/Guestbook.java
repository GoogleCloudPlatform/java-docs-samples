/*
 * Copyright 2016 Google Inc.
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

import com.example.time.Clock;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import java.util.Date;
import java.util.List;

/**
 * A log of notes left by users.
 *
 * <p>This demonstrates the use of Google Cloud Datastore using the App Engine APIs. See the <a
 * href="https://cloud.google.com/appengine/docs/java/datastore/">documentation</a> for more
 * information.
 */
class Guestbook extends AbstractGuestbook {

  Guestbook(Clock clock) {
    super(clock);
  }

  @Override
  protected Entity createGreeting(
      DatastoreService datastore, User user, Date date, String content) {
    // No parent key specified, so Greeting is a root entity.
    Entity greeting = new Entity("Greeting");
    greeting.setProperty("user", user);
    greeting.setProperty("date", date);
    greeting.setProperty("content", content);

    datastore.put(greeting);
    return greeting;
  }

  @Override
  protected List<Entity> listGreetingEntities(DatastoreService datastore) {
    Query query = new Query("Greeting").addSort("date", Query.SortDirection.DESCENDING);
    return datastore.prepare(query).asList(FetchOptions.Builder.withLimit(10));
  }
}
