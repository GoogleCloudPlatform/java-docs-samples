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

import static com.google.common.truth.Truth.assertThat;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

/**
 * Unit tests to demonstrate App Engine Datastore queries.
 */
@RunWith(JUnit4.class)
public class IndexesTest {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          // Set no eventual consistency, that way queries return all results.
          // https://cloud.google.com/appengine/docs/java/tools/localunittesting#Java_Writing_High_Replication_Datastore_tests
          new LocalDatastoreServiceTestConfig()
              .setDefaultHighRepJobPolicyUnappliedJobPercentage(0));

  private DatastoreService datastore;

  @Before
  public void setUp() {
    helper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void propertyFilterExample_returnsMatchingEntities() throws Exception {
    // [START unindexed_properties_1]
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Key acmeKey = KeyFactory.createKey("Company", "Acme");

    Entity tom = new Entity("Person", "Tom", acmeKey);
    tom.setProperty("name", "Tom");
    tom.setProperty("age", 32);
    datastore.put(tom);

    Entity lucy = new Entity("Person", "Lucy", acmeKey);
    lucy.setProperty("name", "Lucy");
    lucy.setUnindexedProperty("age", 29);
    datastore.put(lucy);

    Filter ageFilter = new FilterPredicate("age", FilterOperator.GREATER_THAN, 25);

    Query q = new Query("Person").setAncestor(acmeKey).setFilter(ageFilter);

    // Returns tom but not lucy, because her age is unindexed
    List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    // [END unindexed_properties_1]

    assertThat(results).named("query results").containsExactly(tom);
  }
}
