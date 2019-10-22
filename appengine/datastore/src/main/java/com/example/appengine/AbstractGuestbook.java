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
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.ImmutableList;
import java.util.Date;
import java.util.List;

/**
 * A log of notes left by users.
 *
 * <p>This is meant to be subclassed to demonstrate different storage structures in Datastore.
 */
abstract class AbstractGuestbook {
  private final DatastoreService datastore;
  private final UserService userService;
  private final Clock clock;

  AbstractGuestbook(Clock clock) {
    this.datastore = DatastoreServiceFactory.getDatastoreService();
    this.userService = UserServiceFactory.getUserService();
    this.clock = clock;
  }

  /**
   * Appends a new greeting to the guestbook and returns the {@link Entity} that was created.
   */
  public Greeting appendGreeting(String content) {
    Greeting greeting =
        Greeting.create(
            createGreeting(
                datastore,
                userService.getCurrentUser(),
                clock.now().toDate(),
                content));
    return greeting;
  }

  /**
   * Write a greeting to Datastore.
   */
  protected abstract Entity createGreeting(
      DatastoreService datastore, User user, Date date, String content);

  /**
   * Return a list of the most recent greetings.
   */
  public List<Greeting> listGreetings() {
    ImmutableList.Builder<Greeting> greetings = ImmutableList.builder();
    for (Entity entity : listGreetingEntities(datastore)) {
      greetings.add(Greeting.create(entity));
    }
    return greetings.build();
  }

  /**
   * Return a list of the most recent greetings.
   */
  protected abstract List<Entity> listGreetingEntities(DatastoreService datastore);
}
