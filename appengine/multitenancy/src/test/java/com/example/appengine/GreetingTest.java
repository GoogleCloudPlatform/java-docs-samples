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

import static com.example.appengine.GuestbookTestUtilities.cleanDatastore;
import static org.junit.Assert.assertEquals;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class GreetingTest {
  private static final String TEST_CONTENT = "The world is Blue today";

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          // Set no eventual consistency, that way queries return all results.
          // https://cloud.google.com/appengine/docs/java/tools/localunittesting#Java_Writing_High_Replication_Datastore_tests
          new LocalDatastoreServiceTestConfig()
              .setDefaultHighRepJobPolicyUnappliedJobPercentage(0));

  private Closeable closeable;
  private DatastoreService ds;

  @Before
  public void setUp() throws Exception {

    helper.setUp();
    ds = DatastoreServiceFactory.getDatastoreService();

    ObjectifyService.register(Guestbook.class);
    ObjectifyService.register(Greeting.class);

    closeable = ObjectifyService.begin();

    cleanDatastore(ds, "default");
  }

  @After
  public void tearDown() {
    cleanDatastore(ds, "default");
    helper.tearDown();
    closeable.close();
  }

  @Test
  public void createSaveObject() throws Exception {

    Greeting g = new Greeting("default", TEST_CONTENT);
    ObjectifyService.ofy().save().entity(g).now();

    Query query = new Query("Greeting")
        .setAncestor(new KeyFactory.Builder("Guestbook", "default").getKey());
    PreparedQuery pq = ds.prepare(query);
    Entity greeting = pq.asSingleEntity();    // Should only be one at this point.
    assertEquals(greeting.getProperty("content"), TEST_CONTENT);
  }
}
