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
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import java.util.ArrayList;
import java.util.List;

public class GuestbookTestUtilities {

  public static void cleanDatastore(DatastoreService ds, String book) {
    Query query =
        new Query("Greeting")
            .setAncestor(new KeyFactory.Builder("Guestbook", book).getKey())
            .setKeysOnly();
    PreparedQuery pq = ds.prepare(query);
    List<Entity> entities = pq.asList(FetchOptions.Builder.withDefaults());
    ArrayList<Key> keys = new ArrayList<>(entities.size());

    for (Entity e : entities) {
      keys.add(e.getKey());
    }
    ds.delete(keys);
  }
}
