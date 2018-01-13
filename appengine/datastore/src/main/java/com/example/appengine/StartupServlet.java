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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A startup handler to populate the datastore with example entities.
 */
public class StartupServlet extends HttpServlet {
  static final String IS_POPULATED_ENTITY = "IsPopulated";
  static final String IS_POPULATED_KEY_NAME = "is-populated";

  private static final String PERSON_ENTITY = "Person";
  private static final String NAME_PROPERTY = "name";
  private static final ImmutableList<String> US_PRESIDENTS =
      ImmutableList.<String>builder()
          .add("George Washington")
          .add("John Adams")
          .add("Thomas Jefferson")
          .add("James Madison")
          .add("James Monroe")
          .add("John Quincy Adams")
          .add("Andrew Jackson")
          .add("Martin Van Buren")
          .add("William Henry Harrison")
          .add("John Tyler")
          .add("James K. Polk")
          .add("Zachary Taylor")
          .add("Millard Fillmore")
          .add("Franklin Pierce")
          .add("James Buchanan")
          .add("Abraham Lincoln")
          .add("Andrew Johnson")
          .add("Ulysses S. Grant")
          .add("Rutherford B. Hayes")
          .add("James A. Garfield")
          .add("Chester A. Arthur")
          .add("Grover Cleveland")
          .add("Benjamin Harrison")
          .add("Grover Cleveland")
          .add("William McKinley")
          .add("Theodore Roosevelt")
          .add("William Howard Taft")
          .add("Woodrow Wilson")
          .add("Warren G. Harding")
          .add("Calvin Coolidge")
          .add("Herbert Hoover")
          .add("Franklin D. Roosevelt")
          .add("Harry S. Truman")
          .add("Dwight D. Eisenhower")
          .add("John F. Kennedy")
          .add("Lyndon B. Johnson")
          .add("Richard Nixon")
          .add("Gerald Ford")
          .add("Jimmy Carter")
          .add("Ronald Reagan")
          .add("George H. W. Bush")
          .add("Bill Clinton")
          .add("George W. Bush")
          .add("Barack Obama")
          .build();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("text/plain");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Key isPopulatedKey = KeyFactory.createKey(IS_POPULATED_ENTITY, IS_POPULATED_KEY_NAME);
    boolean isAlreadyPopulated;
    try {
      datastore.get(isPopulatedKey);
      isAlreadyPopulated = true;
    } catch (EntityNotFoundException expected) {
      isAlreadyPopulated = false;
    }
    if (isAlreadyPopulated) {
      resp.getWriter().println("ok");
      return;
    }

    ImmutableList.Builder<Entity> people = ImmutableList.builder();
    for (String name : US_PRESIDENTS) {
      Entity person = new Entity(PERSON_ENTITY);
      person.setProperty(NAME_PROPERTY, name);
      people.add(person);
    }
    datastore.put(people.build());
    datastore.put(new Entity(isPopulatedKey));
    resp.getWriter().println("ok");
  }
}
