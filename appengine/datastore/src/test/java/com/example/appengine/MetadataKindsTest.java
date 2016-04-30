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
import com.google.appengine.api.datastore.Entities;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests to demonstrate App Engine Datastore kinds metadata.
 */
@RunWith(JUnit4.class)
public class MetadataKindsTest {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          // Set no eventual consistency, that way queries return all results.
          // https://cloud.google.com/appengine/docs/java/tools/localunittesting#Java_Writing_High_Replication_Datastore_tests
          new LocalDatastoreServiceTestConfig()
              .setDefaultHighRepJobPolicyUnappliedJobPercentage(0));

  private StringWriter responseWriter;
  private DatastoreService datastore;

  @Before
  public void setUp() {
    helper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();
    responseWriter = new StringWriter();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  // [START kind_query_example]
  void printLowercaseKinds(DatastoreService ds, PrintWriter writer) {

    // Start with unrestricted kind query
    Query q = new Query(Entities.KIND_METADATA_KIND);

    List<Filter> subFils = new ArrayList();

    // Limit to lowercase initial letters
    subFils.add(
        new FilterPredicate(
            Entity.KEY_RESERVED_PROPERTY,
            FilterOperator.GREATER_THAN_OR_EQUAL,
            Entities.createKindKey("a")));

    String endChar = Character.toString((char) ('z' + 1)); // Character after 'z'

    subFils.add(
        new FilterPredicate(
            Entity.KEY_RESERVED_PROPERTY,
            FilterOperator.LESS_THAN,
            Entities.createKindKey(endChar)));

    q.setFilter(CompositeFilterOperator.and(subFils));

    // Print heading
    writer.println("Lowercase kinds:");

    // Print query results
    for (Entity e : ds.prepare(q).asIterable()) {
      writer.println("  " + e.getKey().getName());
    }
  }
  // [END kind_query_example]

  @Test
  public void printLowercaseKinds_printsKinds() throws Exception {
    datastore.put(new Entity("alpha"));
    datastore.put(new Entity("beta"));
    datastore.put(new Entity("NotIncluded"));
    datastore.put(new Entity("zed"));

    printLowercaseKinds(datastore, new PrintWriter(responseWriter));

    String response = responseWriter.toString();
    assertThat(response).contains("alpha");
    assertThat(response).contains("beta");
    assertThat(response).contains("zed");
    assertThat(response).doesNotContain("NotIncluded");
  }
}
