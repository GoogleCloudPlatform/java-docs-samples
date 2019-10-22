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

package com.example.guestbook;

import static com.example.guestbook.Persistence.getDatastore;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.testing.LocalDatastoreHelper;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;

public class TestUtils {
  static LocalDatastoreHelper datastore = LocalDatastoreHelper.create();

  public static void startDatastore() {
    try {
      datastore.start();
      Persistence.setDatastore(datastore.options().service());
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static void stopDatastore() {
    try {
      datastore.stop();
      Persistence.setDatastore(null);
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static void wipeDatastore() {
    Datastore datastore = getDatastore();
    QueryResults<Key> guestbooks = datastore.run(Query.keyQueryBuilder().kind("Greeting")
        .build());
    ArrayList<Key> keys = Lists.newArrayList(guestbooks);

    if (!keys.isEmpty()) {
      datastore.delete(keys.toArray(new Key[keys.size()]));
    }
  }
}
