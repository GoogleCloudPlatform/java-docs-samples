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
import com.google.appengine.api.datastore.Entities;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests to demonstrate App Engine Datastore properties metadata.
 */
@RunWith(JUnit4.class)
public class MetadataPropertiesTest {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          // Set no eventual consistency, that way queries return all results.
          // https://cloud.google
          // .com/appengine/docs/java/tools/localunittesting
          // #Java_Writing_High_Replication_Datastore_tests
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

  // [START property_query_example]
  void printProperties(DatastoreService ds, PrintWriter writer) {

    // Create unrestricted keys-only property query
    Query q = new Query(Entities.PROPERTY_METADATA_KIND).setKeysOnly();

    // Print query results
    for (Entity e : ds.prepare(q).asIterable()) {
      writer.println(e.getKey().getParent().getName() + ": " + e.getKey().getName());
    }
  }
  // [END property_query_example]

  @Test
  public void printProperties_printsProperties() throws Exception {
    Entity a = new Entity("Widget");
    a.setProperty("combobulators", 2);
    a.setProperty("oscillatorState", "harmonzing");
    Entity b = new Entity("Ship");
    b.setProperty("sails", 2);
    b.setProperty("captain", "Blackbeard");
    Entity c = new Entity("Ship");
    c.setProperty("captain", "Redbeard");
    c.setProperty("motor", "outboard");
    datastore.put(Arrays.asList(a, b, c));

    printProperties(datastore, new PrintWriter(responseWriter));

    String response = responseWriter.toString();
    assertThat(response).contains("Widget: combobulators");
    assertThat(response).contains("Widget: oscillatorState");
    assertThat(response).contains("Ship: sails");
    assertThat(response).contains("Ship: captain");
    assertThat(response).contains("Ship: motor");
  }

  // [START property_filtering_example]
  void printPropertyRange(DatastoreService ds, PrintWriter writer) {

    // Start with unrestricted keys-only property query
    Query q = new Query(Entities.PROPERTY_METADATA_KIND).setKeysOnly();

    // Limit range
    q.setFilter(
        CompositeFilterOperator.and(
            new FilterPredicate(
                Entity.KEY_RESERVED_PROPERTY,
                Query.FilterOperator.GREATER_THAN_OR_EQUAL,
                Entities.createPropertyKey("Employee", "salary")),
            new FilterPredicate(
                Entity.KEY_RESERVED_PROPERTY,
                Query.FilterOperator.LESS_THAN_OR_EQUAL,
                Entities.createPropertyKey("Manager", "salary"))));
    q.addSort(Entity.KEY_RESERVED_PROPERTY, SortDirection.ASCENDING);

    // Print query results
    for (Entity e : ds.prepare(q).asIterable()) {
      writer.println(e.getKey().getParent().getName() + ": " + e.getKey().getName());
    }
  }
  // [END property_filtering_example]

  @Test
  public void printPropertyRange_printsProperties() throws Exception {
    Entity account = new Entity("Account");
    account.setProperty("balance", "10.30");
    account.setProperty("company", "General Company");
    Entity employee = new Entity("Employee");
    employee.setProperty("name", "John Doe");
    employee.setProperty("ssn", "987-65-4321");
    Entity invoice = new Entity("Invoice");
    invoice.setProperty("date", new Date());
    invoice.setProperty("amount", "99.98");
    Entity manager = new Entity("Manager");
    manager.setProperty("name", "Jane Doe");
    manager.setProperty("title", "Technical Director");
    Entity product = new Entity("Product");
    product.setProperty("description", "Widget to re-ionize an oscillator");
    product.setProperty("price", "19.97");
    datastore.put(Arrays.asList(account, employee, invoice, manager, product));

    printPropertyRange(datastore, new PrintWriter(responseWriter));

    String response = responseWriter.toString();
    assertThat(response)
        .isEqualTo("Employee: ssn\nInvoice: amount\nInvoice: date\nManager: name\n");
  }

  // [START property_ancestor_query_example]
  List<String> propertiesOfKind(DatastoreService ds, String kind) {

    // Start with unrestricted keys-only property query
    Query q = new Query(Entities.PROPERTY_METADATA_KIND).setKeysOnly();

    // Limit to specified kind
    q.setAncestor(Entities.createKindKey(kind));

    // Initialize result list
    ArrayList<String> results = new ArrayList<String>();

    //Build list of query results
    for (Entity e : ds.prepare(q).asIterable()) {
      results.add(e.getKey().getName());
    }

    // Return result list
    return results;
  }
  // [END property_ancestor_query_example]

  @Test
  public void propertiesOfKind_returnsProperties() throws Exception {
    Entity a = new Entity("Alpha");
    a.setProperty("beta", 12);
    a.setProperty("charlie", "misc.");
    Entity b = new Entity("Alpha");
    b.setProperty("charlie", "assorted");
    b.setProperty("delta", new Date());
    Entity c = new Entity("Charlie");
    c.setProperty("charlie", "some");
    c.setProperty("echo", new Date());
    datastore.put(Arrays.asList(a, b, c));

    List<String> properties = propertiesOfKind(datastore, "Alpha");

    assertThat(properties).containsExactly("beta", "charlie", "delta");
  }

  // [START property_representation_query_example]
  Collection<String> representationsOfProperty(DatastoreService ds, String kind, String property) {

    // Start with unrestricted non-keys-only property query
    Query q = new Query(Entities.PROPERTY_METADATA_KIND);

    // Limit to specified kind and property
    q.setFilter(
        new FilterPredicate(
            "__key__", Query.FilterOperator.EQUAL, Entities.createPropertyKey(kind, property)));

    // Get query result
    Entity propInfo = ds.prepare(q).asSingleEntity();

    // Return collection of property representations
    return (Collection<String>) propInfo.getProperty("property_representation");
  }
  // [END property_representation_query_example]

  @Test
  public void representationsOfProperty_returnsRepresentations() throws Exception {
    Entity a = new Entity("Alpha");
    a.setProperty("beta", 12);
    Entity b = new Entity("Alpha");
    b.setProperty("beta", true);
    Entity c = new Entity("Alpha");
    c.setProperty("beta", new Date());
    datastore.put(Arrays.asList(a, b, c));

    Collection<String> results = representationsOfProperty(datastore, "Alpha", "beta");

    assertThat(results).containsExactly("INT64", "BOOLEAN");
  }
}
