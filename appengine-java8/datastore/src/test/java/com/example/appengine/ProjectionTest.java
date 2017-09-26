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

import static com.google.common.truth.Truth.assertThat;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests to demonstrate App Engine Datastore projection queries.
 */
@RunWith(JUnit4.class)
public class ProjectionTest {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          // Set no eventual consistency, that way queries return all results.
          // https://cloud.google
          // .com/appengine/docs/java/tools/localunittesting
          // #Java_Writing_High_Replication_Datastore_tests
          new LocalDatastoreServiceTestConfig()
              .setDefaultHighRepJobPolicyUnappliedJobPercentage(0));

  private DatastoreService datastore;

  @Before
  public void setUp() throws Exception {
    helper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void projectionQuery_grouping_filtersDuplicates() {
    putTestData("some duplicate", 0L);
    putTestData("some duplicate", 0L);
    putTestData("too big", 1L);

    // [START grouping]
    Query q = new Query("TestKind");
    q.addProjection(new PropertyProjection("A", String.class));
    q.addProjection(new PropertyProjection("B", Long.class));
    q.setDistinct(true);
    q.setFilter(Query.FilterOperator.LESS_THAN.of("B", 1L));
    q.addSort("B", Query.SortDirection.DESCENDING);
    q.addSort("A");
    // [END grouping]

    List<Entity> entities = datastore.prepare(q).asList(FetchOptions.Builder.withLimit(5));
    assertThat(entities).hasSize(1);
    Entity entity = entities.get(0);
    assertThat((String) entity.getProperty("A")).named("entity.A").isEqualTo("some duplicate");
    assertThat((long) entity.getProperty("B")).named("entity.B").isEqualTo(0L);
  }

  private void putTestData(String a, long b) {
    Entity entity = new Entity("TestKind");
    entity.setProperty("A", a);
    entity.setProperty("B", b);
    datastore.put(entity);
  }
}
