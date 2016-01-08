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

import com.google.gcloud.datastore.Datastore;
import com.google.gcloud.datastore.Entity;
import com.google.gcloud.datastore.FullEntity;
import com.google.gcloud.datastore.Key;
import com.google.gcloud.datastore.KeyFactory;
import com.google.gcloud.datastore.Query;
import com.google.gcloud.datastore.QueryResults;

import java.util.ArrayList;
import java.util.List;

public class UserService {

  private static final String KINDNAME = "DEMO_USER";
  private Datastore datastore;

  public UserService(Datastore datastore) {
    this.datastore = datastore;
  }

  public List<User> getAllUsers() {
    Query<Entity> query =
        Query.gqlQueryBuilder(Query.ResultType.ENTITY, "SELECT * FROM " + KINDNAME).build();
    QueryResults<Entity> results = datastore.run(query);
    List<User> users = new ArrayList<>();
    while (results.hasNext()) {
      Entity result = results.next();
      users.add(
          new User(result.getString("id"), result.getString("name"), result.getString("email")));
    }
    return users;
  }

  public User createUser(String name, String email) {
    failIfInvalid(name, email);
    User user = new User(name, email);
    KeyFactory keyFactory = datastore.newKeyFactory().kind(KINDNAME);
    Key key = keyFactory.newKey(user.getId());
    FullEntity entity = Entity.builder(key)
        .set("id", user.getId())
        .set("name", name)
        .set("email", email)
        .build();
    datastore.add(entity);
    return user;
  }

  public String deleteUser(String id) {
    KeyFactory keyFactory = datastore.newKeyFactory().kind(KINDNAME);
    Key key = keyFactory.newKey(id);
    datastore.delete(key);
    return "ok";
  }

  public User updateUser(String id, String name, String email) {
    failIfInvalid(name, email);
    KeyFactory keyFactory = datastore.newKeyFactory().kind(KINDNAME);
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
