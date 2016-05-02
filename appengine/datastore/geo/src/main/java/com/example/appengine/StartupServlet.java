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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

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

    // [START create_entity_with_geopt_property]
    Entity station = new Entity("GasStation");
    station.setProperty("brand", "Ocean Ave Shell");
    station.setProperty("location", new GeoPt(37.7913156f, -122.3926051f));
    datastore.put(station);
    // [END create_entity_with_geopt_property]

    station = new Entity("GasStation");
    station.setProperty("brand", "Charge Point Charging Station");
    station.setProperty("location", new GeoPt(37.7909778f, -122.3929963f));
    datastore.put(station);

    station = new Entity("GasStation");
    station.setProperty("brand", "76");
    station.setProperty("location", new GeoPt(37.7860533f, -122.3940325f));
    datastore.put(station);

    datastore.put(new Entity(isPopulatedKey));
    resp.getWriter().println("ok");
  }
}
