/*
 * Copyright (c) 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.google.appengine.sparkdemo;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;

import java.util.ArrayList;
import java.util.List;

public class UserService {

  private final Datastore datastore;
  private final KeyFactory keyFactory;
  private final String kind;

  /**
   * Constructor for UserService.
   *
   * @param datastore gcloud-java Datastore service object to execute requests
   * @param kind the kind for the Datastore entities in this demo
   */
  public UserService(Datastore datastore, String kind) {
    this.datastore = datastore;
    this.keyFactory = datastore.newKeyFactory().kind(kind);
    this.kind = kind;
  }

  /**
   * Return a list of all users.
   */
  public List<User> getAllUsers() {
    Query<Entity> query =
        Query.gqlQueryBuilder(Query.ResultType.ENTITY, "SELECT * FROM " + kind).build();
    QueryResults<Entity> results = datastore.run(query);
    List<User> users = new ArrayList<>();
    while (results.hasNext()) {
      Entity result = results.next();
      users.add(
          new User(result.getString("id"), result.getString("name"), result.getString("email")));
    }
    return users;
  }

  /**
   * Return the user with the given id.
   */
  User getUser(String id) {
    Entity entity = datastore.get(keyFactory.newKey(id));
    return entity == null
        ? null
        : new User(entity.getString("id"), entity.getString("name"), entity.getString("email"));
  }

  /**
   * Create a new user and add it to Cloud Datastore.
   */
  public User createUser(String name, String email) {
    failIfInvalid(name, email);
    User user = new User(name, email);
    Key key = keyFactory.newKey(user.getId());
    Entity entity = Entity.builder(key)
        .set("id", user.getId())
        .set("name", name)
        .set("email", email)
        .build();
    datastore.add(entity);
    return user;
  }

  /**
   * Delete a user from Cloud Datastore.
   */
  public String deleteUser(String id) {
    Key key = keyFactory.newKey(id);
    datastore.delete(key);
    return "ok";
  }

  /**
   * Updates a user in Cloud Datastore.
   */
  public User updateUser(String id, String name, String email) {
    failIfInvalid(name, email);
    Key key = keyFactory.newKey(id);
    Entity entity = datastore.get(key);
    if (entity == null) {
      throw new IllegalArgumentException("No user with id '" + id + "' found");
    } else {
      entity = Entity.builder(entity)
          .set("id", id)
          .set("name", name)
          .set("email", email)
          .build();
      datastore.update(entity);
    }
    return new User(id, name, email);
  }

  private void failIfInvalid(String name, String email) {
    checkArgument(name != null && !name.isEmpty(), "Parameter 'name' cannot be empty");
    checkArgument(email != null && !email.isEmpty(), "Parameter 'email' cannot be empty");
  }
}
