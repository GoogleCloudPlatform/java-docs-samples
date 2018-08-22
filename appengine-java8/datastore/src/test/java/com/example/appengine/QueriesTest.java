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
import static org.junit.Assert.fail;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.ImmutableList;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests to demonstrate App Engine Datastore queries.
 */
@RunWith(JUnit4.class)
public class QueriesTest {

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
    // Arrange
    Entity p1 = new Entity("Person");
    p1.setProperty("height", 120);
    Entity p2 = new Entity("Person");
    p2.setProperty("height", 180);
    Entity p3 = new Entity("Person");
    p3.setProperty("height", 160);
    datastore.put(ImmutableList.<Entity>of(p1, p2, p3));

    // Act
    long minHeight = 160;
    // [START gae_java8_datastore_property_filter]
    Filter propertyFilter =
        new FilterPredicate("height", FilterOperator.GREATER_THAN_OR_EQUAL, minHeight);
    Query q = new Query("Person").setFilter(propertyFilter);
    // [END gae_java8_datastore_property_filter]

    // Assert
    List<Entity> results =
        datastore.prepare(q.setKeysOnly()).asList(FetchOptions.Builder.withDefaults());
    assertThat(results).named("query results").containsExactly(p2, p3);
  }

  @Test
  public void keyFilterExample_returnsMatchingEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Person", "a");
    Entity b = new Entity("Person", "b");
    Entity c = new Entity("Person", "c");
    Entity aa = new Entity("Person", "aa", b.getKey());
    Entity bb = new Entity("Person", "bb", b.getKey());
    Entity aaa = new Entity("Person", "aaa", bb.getKey());
    Entity bbb = new Entity("Person", "bbb", bb.getKey());
    datastore.put(ImmutableList.<Entity>of(a, b, c, aa, bb, aaa, bbb));

    // Act
    Key lastSeenKey = bb.getKey();
    // [START gae_java8_datastore_key_filter]
    Filter keyFilter =
        new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.GREATER_THAN, lastSeenKey);
    Query q = new Query("Person").setFilter(keyFilter);
    // [END gae_java8_datastore_key_filter]

    // Assert
    List<Entity> results =
        datastore.prepare(q.setKeysOnly()).asList(FetchOptions.Builder.withDefaults());
    assertThat(results)
        .named("query results")
        .containsExactly(
            aaa, // Ancestor path "b/bb/aaa" is greater than "b/bb".
            bbb, // Ancestor path "b/bb/bbb" is greater than "b/bb".
            c); // Key name identifier "c" is greater than b.
  }

  @Test
  public void keyFilterExample_kindless_returnsMatchingEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Child", "a");
    Entity b = new Entity("Child", "b");
    Entity c = new Entity("Child", "c");
    Entity aa = new Entity("Child", "aa", b.getKey());
    Entity bb = new Entity("Child", "bb", b.getKey());
    Entity aaa = new Entity("Child", "aaa", bb.getKey());
    Entity bbb = new Entity("Child", "bbb", bb.getKey());
    Entity adult = new Entity("Adult", "a");
    Entity zooAnimal = new Entity("ZooAnimal", "a");
    datastore.put(ImmutableList.<Entity>of(a, b, c, aa, bb, aaa, bbb, adult, zooAnimal));

    // Act
    Key lastSeenKey = bb.getKey();
    // [START gae_java8_datastore_kindless_query]
    Filter keyFilter =
        new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.GREATER_THAN, lastSeenKey);
    Query q = new Query().setFilter(keyFilter);
    // [END gae_java8_datastore_kindless_query]

    // Assert
    List<Entity> results =
        datastore.prepare(q.setKeysOnly()).asList(FetchOptions.Builder.withDefaults());
    assertThat(results)
        .named("query results")
        .containsExactly(
            aaa, // Ancestor path "b/bb/aaa" is greater than "b/bb".
            bbb, // Ancestor path "b/bb/bbb" is greater than "b/bb".
            zooAnimal, // Kind "ZooAnimal" is greater than "Child"
            c); // Key name identifier "c" is greater than b.
  }

  @Test
  public void ancestorFilterExample_returnsMatchingEntities() throws Exception {
    Entity a = new Entity("Person", "a");
    Entity b = new Entity("Person", "b");
    Entity aa = new Entity("Person", "aa", a.getKey());
    Entity ab = new Entity("Person", "ab", a.getKey());
    Entity bb = new Entity("Person", "bb", b.getKey());
    datastore.put(ImmutableList.<Entity>of(a, b, aa, ab, bb));

    Key ancestorKey = a.getKey();
    // [START gae_java8_datastore_ancestor_filter]
    Query q = new Query("Person").setAncestor(ancestorKey);
    // [END gae_java8_datastore_ancestor_filter]

    // Assert
    List<Entity> results =
        datastore.prepare(q.setKeysOnly()).asList(FetchOptions.Builder.withDefaults());
    assertThat(results).named("query results").containsExactly(a, aa, ab);
  }

  @Test
  public void ancestorQueryExample_returnsMatchingEntities() throws Exception {
    // [START gae_java8_datastore_ancestor_query]
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity tom = new Entity("Person", "Tom");
    Key tomKey = tom.getKey();
    datastore.put(tom);

    Entity weddingPhoto = new Entity("Photo", tomKey);
    weddingPhoto.setProperty("imageURL", "http://domain.com/some/path/to/wedding_photo.jpg");

    Entity babyPhoto = new Entity("Photo", tomKey);
    babyPhoto.setProperty("imageURL", "http://domain.com/some/path/to/baby_photo.jpg");

    Entity dancePhoto = new Entity("Photo", tomKey);
    dancePhoto.setProperty("imageURL", "http://domain.com/some/path/to/dance_photo.jpg");

    Entity campingPhoto = new Entity("Photo");
    campingPhoto.setProperty("imageURL", "http://domain.com/some/path/to/camping_photo.jpg");

    List<Entity> photoList = Arrays.asList(weddingPhoto, babyPhoto, dancePhoto, campingPhoto);
    datastore.put(photoList);

    Query photoQuery = new Query("Photo").setAncestor(tomKey);

    // This returns weddingPhoto, babyPhoto, and dancePhoto,
    // but not campingPhoto, because tom is not an ancestor
    List<Entity> results =
        datastore.prepare(photoQuery).asList(FetchOptions.Builder.withDefaults());
    // [END gae_java8_datastore_ancestor_query]

    assertThat(results).named("query results").containsExactly(weddingPhoto, babyPhoto, dancePhoto);
  }

  @Test
  public void ancestorQueryExample_kindlessKeyFilter_returnsMatchingEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Grandparent", "a");
    Entity b = new Entity("Grandparent", "b");
    Entity c = new Entity("Grandparent", "c");
    Entity aa = new Entity("Parent", "aa", a.getKey());
    Entity ba = new Entity("Parent", "ba", b.getKey());
    Entity bb = new Entity("Parent", "bb", b.getKey());
    Entity bc = new Entity("Parent", "bc", b.getKey());
    Entity cc = new Entity("Parent", "cc", c.getKey());
    Entity aaa = new Entity("Child", "aaa", aa.getKey());
    Entity bbb = new Entity("Child", "bbb", bb.getKey());
    datastore.put(ImmutableList.<Entity>of(a, b, c, aa, ba, bb, bc, cc, aaa, bbb));

    // Act
    Key ancestorKey = b.getKey();
    Key lastSeenKey = bb.getKey();
    // [START gae_java8_datastore_kindless_ancestor_key_query]
    Filter keyFilter =
        new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.GREATER_THAN, lastSeenKey);
    Query q = new Query().setAncestor(ancestorKey).setFilter(keyFilter);
    // [END gae_java8_datastore_kindless_ancestor_key_query]

    // Assert
    List<Entity> results =
        datastore.prepare(q.setKeysOnly()).asList(FetchOptions.Builder.withDefaults());
    assertThat(results).named("query results").containsExactly(bc, bbb);
  }

  @Test
  public void ancestorQueryExample_kindlessKeyFilterFull_returnsMatchingEntities()
      throws Exception {
    // [START gae_java8_datastore_kindless_ancestor_query]
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity tom = new Entity("Person", "Tom");
    Key tomKey = tom.getKey();
    datastore.put(tom);

    Entity weddingPhoto = new Entity("Photo", tomKey);
    weddingPhoto.setProperty("imageURL", "http://domain.com/some/path/to/wedding_photo.jpg");

    Entity weddingVideo = new Entity("Video", tomKey);
    weddingVideo.setProperty("videoURL", "http://domain.com/some/path/to/wedding_video.avi");

    List<Entity> mediaList = Arrays.asList(weddingPhoto, weddingVideo);
    datastore.put(mediaList);

    // By default, ancestor queries include the specified ancestor itself.
    // The following filter excludes the ancestor from the query results.
    Filter keyFilter =
        new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.GREATER_THAN, tomKey);

    Query mediaQuery = new Query().setAncestor(tomKey).setFilter(keyFilter);

    // Returns both weddingPhoto and weddingVideo,
    // even though they are of different entity kinds
    List<Entity> results =
        datastore.prepare(mediaQuery).asList(FetchOptions.Builder.withDefaults());
    // [END gae_java8_datastore_kindless_ancestor_query]

    assertThat(results).named("query result keys").containsExactly(weddingPhoto, weddingVideo);
  }

  @Test
  public void keysOnlyExample_returnsMatchingEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Person", "a");
    Entity b = new Entity("Building", "b");
    Entity c = new Entity("Person", "c");
    datastore.put(ImmutableList.<Entity>of(a, b, c));

    // [START gae_java8_datastore_keys_only]
    Query q = new Query("Person").setKeysOnly();
    // [END gae_java8_datastore_keys_only]

    // Assert
    List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    assertThat(results).named("query results").containsExactly(a, c);
  }

  @Test
  public void sortOrderExample_returnsSortedEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Person", "a");
    a.setProperty("lastName", "Alpha");
    a.setProperty("height", 100);
    Entity b = new Entity("Person", "b");
    b.setProperty("lastName", "Bravo");
    b.setProperty("height", 200);
    Entity c = new Entity("Person", "c");
    c.setProperty("lastName", "Charlie");
    c.setProperty("height", 300);
    datastore.put(ImmutableList.<Entity>of(a, b, c));

    // Act
    // [START gae_java8_datastore_sort_order]
    // Order alphabetically by last name:
    Query q1 = new Query("Person").addSort("lastName", SortDirection.ASCENDING);

    // Order by height, tallest to shortest:
    Query q2 = new Query("Person").addSort("height", SortDirection.DESCENDING);
    // [END gae_java8_datastore_sort_order]

    // Assert
    List<Entity> lastNameResults =
        datastore.prepare(q1.setKeysOnly()).asList(FetchOptions.Builder.withDefaults());
    assertThat(lastNameResults).named("last name query results").containsExactly(a, b, c).inOrder();
    List<Entity> heightResults =
        datastore.prepare(q2.setKeysOnly()).asList(FetchOptions.Builder.withDefaults());
    assertThat(heightResults).named("height query results").containsExactly(c, b, a).inOrder();
  }

  @Test
  public void sortOrderExample_multipleSortOrders_returnsSortedEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Person", "a");
    a.setProperty("lastName", "Alpha");
    a.setProperty("height", 100);
    Entity b1 = new Entity("Person", "b1");
    b1.setProperty("lastName", "Bravo");
    b1.setProperty("height", 150);
    Entity b2 = new Entity("Person", "b2");
    b2.setProperty("lastName", "Bravo");
    b2.setProperty("height", 200);
    Entity c = new Entity("Person", "c");
    c.setProperty("lastName", "Charlie");
    c.setProperty("height", 300);
    datastore.put(ImmutableList.<Entity>of(a, b1, b2, c));

    // Act
    // [START gae_java8_datastore_multiple_sort_orders]
    Query q =
        new Query("Person")
            .addSort("lastName", SortDirection.ASCENDING)
            .addSort("height", SortDirection.DESCENDING);
    // [END gae_java8_datastore_multiple_sort_orders]

    // Assert
    List<Entity> results =
        datastore.prepare(q.setKeysOnly()).asList(FetchOptions.Builder.withDefaults());
    assertThat(results).named("query results").containsExactly(a, b2, b1, c).inOrder();
  }

  @Test
  public void queryInterface_multipleFilters_printsMatchedEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Person", "a");
    a.setProperty("firstName", "Alph");
    a.setProperty("lastName", "Alpha");
    a.setProperty("height", 60);
    Entity b = new Entity("Person", "b");
    b.setProperty("firstName", "Bee");
    b.setProperty("lastName", "Bravo");
    b.setProperty("height", 70);
    Entity c = new Entity("Person", "c");
    c.setProperty("firstName", "Charles");
    c.setProperty("lastName", "Charlie");
    c.setProperty("height", 100);
    datastore.put(ImmutableList.<Entity>of(a, b, c));

    StringWriter buf = new StringWriter();
    PrintWriter out = new PrintWriter(buf);
    long minHeight = 60;
    long maxHeight = 72;

    // Act
    // [START gae_java8_datastore_interface_1]
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Filter heightMinFilter =
        new FilterPredicate("height", FilterOperator.GREATER_THAN_OR_EQUAL, minHeight);

    Filter heightMaxFilter =
        new FilterPredicate("height", FilterOperator.LESS_THAN_OR_EQUAL, maxHeight);

    // Use CompositeFilter to combine multiple filters
    CompositeFilter heightRangeFilter =
        CompositeFilterOperator.and(heightMinFilter, heightMaxFilter);

    // Use class Query to assemble a query
    Query q = new Query("Person").setFilter(heightRangeFilter);

    // Use PreparedQuery interface to retrieve results
    PreparedQuery pq = datastore.prepare(q);

    for (Entity result : pq.asIterable()) {
      String firstName = (String) result.getProperty("firstName");
      String lastName = (String) result.getProperty("lastName");
      Long height = (Long) result.getProperty("height");

      out.println(firstName + " " + lastName + ", " + height + " inches tall");
    }
    // [END gae_java8_datastore_interface_1]

    // Assert
    assertThat(buf.toString()).contains("Alph Alpha, 60 inches tall");
    assertThat(buf.toString()).contains("Bee Bravo, 70 inches tall");
    assertThat(buf.toString()).doesNotContain("Charlie");
  }

  @Test
  public void queryInterface_singleFilter_returnsMatchedEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Person", "a");
    a.setProperty("height", 100);
    Entity b = new Entity("Person", "b");
    b.setProperty("height", 150);
    Entity c = new Entity("Person", "c");
    c.setProperty("height", 300);
    datastore.put(ImmutableList.<Entity>of(a, b, c));

    // Act
    long minHeight = 150;
    // [START gae_java8_datastore_interface_2]
    Filter heightMinFilter =
        new FilterPredicate("height", FilterOperator.GREATER_THAN_OR_EQUAL, minHeight);

    Query q = new Query("Person").setFilter(heightMinFilter);
    // [END gae_java8_datastore_interface_2]

    // Assert
    List<Entity> results =
        datastore.prepare(q.setKeysOnly()).asList(FetchOptions.Builder.withDefaults());
    assertThat(results).named("query results").containsExactly(b, c);
  }

  @Test
  public void queryInterface_orFilter_printsMatchedEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Person", "a");
    a.setProperty("height", 100);
    Entity b = new Entity("Person", "b");
    b.setProperty("height", 150);
    Entity c = new Entity("Person", "c");
    c.setProperty("height", 200);
    datastore.put(ImmutableList.<Entity>of(a, b, c));

    StringWriter buf = new StringWriter();
    PrintWriter out = new PrintWriter(buf);
    long minHeight = 125;
    long maxHeight = 175;

    // Act
    // [START gae_java8_datastore_interface_3]
    Filter tooShortFilter = new FilterPredicate("height", FilterOperator.LESS_THAN, minHeight);

    Filter tooTallFilter = new FilterPredicate("height", FilterOperator.GREATER_THAN, maxHeight);

    Filter heightOutOfRangeFilter = CompositeFilterOperator.or(tooShortFilter, tooTallFilter);

    Query q = new Query("Person").setFilter(heightOutOfRangeFilter);
    // [END gae_java8_datastore_interface_3]

    // Assert
    List<Entity> results =
        datastore.prepare(q.setKeysOnly()).asList(FetchOptions.Builder.withDefaults());
    assertThat(results).named("query results").containsExactly(a, c);
  }

  @Test
  public void queryRestrictions_compositeFilter_returnsMatchedEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Person", "a");
    a.setProperty("birthYear", 1930);
    Entity b = new Entity("Person", "b");
    b.setProperty("birthYear", 1960);
    Entity c = new Entity("Person", "c");
    c.setProperty("birthYear", 1990);
    datastore.put(ImmutableList.<Entity>of(a, b, c));

    // Act
    long minBirthYear = 1940;
    long maxBirthYear = 1980;
    // [START gae_java8_datastore_inequality_filters_one_property_valid_1]
    Filter birthYearMinFilter =
        new FilterPredicate("birthYear", FilterOperator.GREATER_THAN_OR_EQUAL, minBirthYear);

    Filter birthYearMaxFilter =
        new FilterPredicate("birthYear", FilterOperator.LESS_THAN_OR_EQUAL, maxBirthYear);

    Filter birthYearRangeFilter =
        CompositeFilterOperator.and(birthYearMinFilter, birthYearMaxFilter);

    Query q = new Query("Person").setFilter(birthYearRangeFilter);
    // [END gae_java8_datastore_inequality_filters_one_property_valid_1]

    // Assert
    List<Entity> results =
        datastore.prepare(q.setKeysOnly()).asList(FetchOptions.Builder.withDefaults());
    assertThat(results).named("query results").containsExactly(b);
  }

  @Test
  public void queryRestrictions_compositeFilter_isInvalid() throws Exception {
    long minBirthYear = 1940;
    long maxHeight = 200;
    // [START gae_java8_datastore_inequality_filters_one_property_invalid]
    Filter birthYearMinFilter =
        new FilterPredicate("birthYear", FilterOperator.GREATER_THAN_OR_EQUAL, minBirthYear);

    Filter heightMaxFilter =
        new FilterPredicate("height", FilterOperator.LESS_THAN_OR_EQUAL, maxHeight);

    Filter invalidFilter = CompositeFilterOperator.and(birthYearMinFilter, heightMaxFilter);

    Query q = new Query("Person").setFilter(invalidFilter);
    // [END gae_java8_datastore_inequality_filters_one_property_invalid]

    // Note: The local devserver behavior is different than the production
    // version of Cloud Datastore, so there aren't any assertions we can make
    // in this test.  The query appears to work with the local test runner,
    // but will fail in production.
  }

  @Test
  public void queryRestrictions_compositeEqualFilter_returnsMatchedEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Person", "a");
    a.setProperty("birthYear", 1930);
    a.setProperty("city", "Somewhere");
    a.setProperty("lastName", "Someone");
    Entity b = new Entity("Person", "b");
    b.setProperty("birthYear", 1960);
    b.setProperty("city", "Somewhere");
    b.setProperty("lastName", "Someone");
    Entity c = new Entity("Person", "c");
    c.setProperty("birthYear", 1990);
    c.setProperty("city", "Somewhere");
    c.setProperty("lastName", "Someone");
    Entity d = new Entity("Person", "d");
    d.setProperty("birthYear", 1960);
    d.setProperty("city", "Nowhere");
    d.setProperty("lastName", "Someone");
    Entity e = new Entity("Person", "e");
    e.setProperty("birthYear", 1960);
    e.setProperty("city", "Somewhere");
    e.setProperty("lastName", "Noone");
    datastore.put(ImmutableList.<Entity>of(a, b, c, d, e));
    long minBirthYear = 1940;
    long maxBirthYear = 1980;
    String targetCity = "Somewhere";
    String targetLastName = "Someone";

    // [START gae_java8_datastore_inequality_filters_one_property_valid_2]
    Filter lastNameFilter = new FilterPredicate("lastName", FilterOperator.EQUAL, targetLastName);

    Filter cityFilter = new FilterPredicate("city", FilterOperator.EQUAL, targetCity);

    Filter birthYearMinFilter =
        new FilterPredicate("birthYear", FilterOperator.GREATER_THAN_OR_EQUAL, minBirthYear);

    Filter birthYearMaxFilter =
        new FilterPredicate("birthYear", FilterOperator.LESS_THAN_OR_EQUAL, maxBirthYear);

    Filter validFilter =
        CompositeFilterOperator.and(
            lastNameFilter, cityFilter, birthYearMinFilter, birthYearMaxFilter);

    Query q = new Query("Person").setFilter(validFilter);
    // [END gae_java8_datastore_inequality_filters_one_property_valid_2]

    // Assert
    List<Entity> results =
        datastore.prepare(q.setKeysOnly()).asList(FetchOptions.Builder.withDefaults());
    assertThat(results).named("query results").containsExactly(b);
  }

  @Test
  public void queryRestrictions_inequalitySortedFirst_returnsMatchedEntities() throws Exception {
    // Arrange
    Entity a = new Entity("Person", "a");
    a.setProperty("birthYear", 1930);
    a.setProperty("lastName", "Someone");
    Entity b = new Entity("Person", "b");
    b.setProperty("birthYear", 1990);
    b.setProperty("lastName", "Bravo");
    Entity c = new Entity("Person", "c");
    c.setProperty("birthYear", 1960);
    c.setProperty("lastName", "Charlie");
    Entity d = new Entity("Person", "d");
    d.setProperty("birthYear", 1960);
    d.setProperty("lastName", "Delta");
    datastore.put(ImmutableList.<Entity>of(a, b, c, d));
    long minBirthYear = 1940;

    // [START gae_java8_datastore_inequality_filters_sort_orders_valid]
    Filter birthYearMinFilter =
        new FilterPredicate("birthYear", FilterOperator.GREATER_THAN_OR_EQUAL, minBirthYear);

    Query q =
        new Query("Person")
            .setFilter(birthYearMinFilter)
            .addSort("birthYear", SortDirection.ASCENDING)
            .addSort("lastName", SortDirection.ASCENDING);
    // [END gae_java8_datastore_inequality_filters_sort_orders_valid]

    // Assert
    List<Entity> results =
        datastore.prepare(q.setKeysOnly()).asList(FetchOptions.Builder.withDefaults());
    assertThat(results).named("query results").containsExactly(c, d, b).inOrder();
  }

  @Test
  public void queryRestrictions_missingSortOnInequality_isInvalid() throws Exception {
    long minBirthYear = 1940;
    // [START gae_java8_datastore_inequality_filters_sort_orders_invalid_1]
    Filter birthYearMinFilter =
        new FilterPredicate("birthYear", FilterOperator.GREATER_THAN_OR_EQUAL, minBirthYear);

    // Not valid. Missing sort on birthYear.
    Query q =
        new Query("Person")
            .setFilter(birthYearMinFilter)
            .addSort("lastName", SortDirection.ASCENDING);
    // [END gae_java8_datastore_inequality_filters_sort_orders_invalid_1]

    // Note: The local devserver behavior is different than the production
    // version of Cloud Datastore, so there aren't any assertions we can make
    // in this test.  The query appears to work with the local test runner,
    // but will fail in production.
  }

  @Test
  public void queryRestrictions_sortWrongOrderOnInequality_isInvalid() throws Exception {
    long minBirthYear = 1940;
    // [START gae_java8_datastore_inequality_filters_sort_orders_invalid_2]
    Filter birthYearMinFilter =
        new FilterPredicate("birthYear", FilterOperator.GREATER_THAN_OR_EQUAL, minBirthYear);

    // Not valid. Sort on birthYear needs to be first.
    Query q =
        new Query("Person")
            .setFilter(birthYearMinFilter)
            .addSort("lastName", SortDirection.ASCENDING)
            .addSort("birthYear", SortDirection.ASCENDING);
    // [END gae_java8_datastore_inequality_filters_sort_orders_invalid_2]

    // Note: The local devserver behavior is different than the production
    // version of Cloud Datastore, so there aren't any assertions we can make
    // in this test.  The query appears to work with the local test runner,
    // but will fail in production.
  }

  @Test
  public void queryRestrictions_surprisingMultipleValuesAllMustMatch_returnsNoEntities()
      throws Exception {
    Entity a = new Entity("Widget", "a");
    List<Long> xs = Arrays.asList(1L, 2L);
    a.setProperty("x", xs);
    datastore.put(a);

    // [START gae_java8_datastore_surprising_behavior_1]
    Query q =
        new Query("Widget")
            .setFilter(
                CompositeFilterOperator.and(
                    new FilterPredicate("x", FilterOperator.GREATER_THAN, 1),
                    new FilterPredicate("x", FilterOperator.LESS_THAN, 2)));
    // [END gae_java8_datastore_surprising_behavior_1]

    // Entity "a" will not match because no individual value matches all filters.
    // See the documentation for more details:
    // https://cloud.google.com/appengine/docs/java/datastore/query-restrictions
    // #properties_with_multiple_values_can_behave_in_surprising_ways
    List<Entity> results =
        datastore.prepare(q.setKeysOnly()).asList(FetchOptions.Builder.withDefaults());
    assertThat(results).named("query results").isEmpty();
  }

  @Test
  public void queryRestrictions_surprisingMultipleValuesEquals_returnsMatchedEntities()
      throws Exception {
    Entity a = new Entity("Widget", "a");
    a.setProperty("x", ImmutableList.<Long>of(1L, 2L));
    Entity b = new Entity("Widget", "b");
    b.setProperty("x", ImmutableList.<Long>of(1L, 3L));
    Entity c = new Entity("Widget", "c");
    c.setProperty("x", ImmutableList.<Long>of(-6L, 2L));
    Entity d = new Entity("Widget", "d");
    d.setProperty("x", ImmutableList.<Long>of(-6L, 4L));
    Entity e = new Entity("Widget", "e");
    e.setProperty("x", ImmutableList.<Long>of(1L, 2L, 3L));
    datastore.put(ImmutableList.<Entity>of(a, b, c, d, e));

    // [START gae_java8_datastore_surprising_behavior_2]
    Query q =
        new Query("Widget")
            .setFilter(
                CompositeFilterOperator.and(
                    new FilterPredicate("x", FilterOperator.EQUAL, 1),
                    new FilterPredicate("x", FilterOperator.EQUAL, 2)));
    // [END gae_java8_datastore_surprising_behavior_2]

    // Only "a" and "e" have both 1 and 2 in the "x" array-valued property.
    // See the documentation for more details:
    // https://cloud.google.com/appengine/docs/java/datastore/query-restrictions
    // #properties_with_multiple_values_can_behave_in_surprising_ways
    List<Entity> results =
        datastore.prepare(q.setKeysOnly()).asList(FetchOptions.Builder.withDefaults());
    assertThat(results).named("query results").containsExactly(a, e);
  }

  @Test
  public void queryRestrictions_surprisingMultipleValuesNotEquals_returnsMatchedEntities()
      throws Exception {
    Entity a = new Entity("Widget", "a");
    a.setProperty("x", ImmutableList.<Long>of(1L, 2L));
    Entity b = new Entity("Widget", "b");
    b.setProperty("x", ImmutableList.<Long>of(1L, 3L));
    Entity c = new Entity("Widget", "c");
    c.setProperty("x", ImmutableList.<Long>of(-6L, 2L));
    Entity d = new Entity("Widget", "d");
    d.setProperty("x", ImmutableList.<Long>of(-6L, 4L));
    Entity e = new Entity("Widget", "e");
    e.setProperty("x", ImmutableList.<Long>of(1L));
    datastore.put(ImmutableList.<Entity>of(a, b, c, d, e));

    // [START gae_java8_datastore_surprising_behavior_3]
    Query q = new Query("Widget").setFilter(new FilterPredicate("x", FilterOperator.NOT_EQUAL, 1));
    // [END gae_java8_datastore_surprising_behavior_3]

    // The query matches any entity that has a some value other than 1. Only
    // entity "e" is not matched.  See the documentation for more details:
    // https://cloud.google.com/appengine/docs/java/datastore/query-restrictions
    // #properties_with_multiple_values_can_behave_in_surprising_ways
    List<Entity> results =
        datastore.prepare(q.setKeysOnly()).asList(FetchOptions.Builder.withDefaults());
    assertThat(results).named("query results").containsExactly(a, b, c, d);
  }

  @Test
  public void queryRestrictions_surprisingMultipleValuesTwoNotEquals_returnsMatchedEntities()
      throws Exception {
    Entity a = new Entity("Widget", "a");
    a.setProperty("x", ImmutableList.<Long>of(1L, 2L));
    Entity b = new Entity("Widget", "b");
    b.setProperty("x", ImmutableList.<Long>of(1L, 2L, 3L));
    datastore.put(ImmutableList.<Entity>of(a, b));

    // [START gae_java8_datastore_surprising_behavior_4]
    Query q =
        new Query("Widget")
            .setFilter(
                CompositeFilterOperator.and(
                    new FilterPredicate("x", FilterOperator.NOT_EQUAL, 1),
                    new FilterPredicate("x", FilterOperator.NOT_EQUAL, 2)));
    // [END gae_java8_datastore_surprising_behavior_4]

    // The two NOT_EQUAL filters in the query become like the combination of queries:
    // x < 1 OR (x > 1 AND x < 2) OR x > 2
    //
    // Only "b" has some value which matches the "x > 2" portion of this query.
    //
    // See the documentation for more details:
    // https://cloud.google.com/appengine/docs/java/datastore/query-restrictions
    // #properties_with_multiple_values_can_behave_in_surprising_ways
    List<Entity> results =
        datastore.prepare(q.setKeysOnly()).asList(FetchOptions.Builder.withDefaults());
    assertThat(results).named("query results").containsExactly(b);
  }

  private Entity retrievePersonWithLastName(String targetLastName) {
    // [START gae_java8_datastore_single_retrieval]
    Query q =
        new Query("Person")
            .setFilter(new FilterPredicate("lastName", FilterOperator.EQUAL, targetLastName));

    PreparedQuery pq = datastore.prepare(q);
    Entity result = pq.asSingleEntity();
    // [END gae_java8_datastore_single_retrieval]
    return result;
  }

  @Test
  public void singleRetrievalExample_singleEntity_returnsEntity() throws Exception {
    Entity a = new Entity("Person", "a");
    a.setProperty("lastName", "Johnson");
    Entity b = new Entity("Person", "b");
    b.setProperty("lastName", "Smith");
    datastore.put(ImmutableList.<Entity>of(a, b));

    Entity result = retrievePersonWithLastName("Johnson");

    assertThat(result).named("result").isEqualTo(a); // Note: Entity.equals() only checks the Key.
  }

  @Test
  public void singleRetrievalExample_multitpleEntities_throwsException() throws Exception {
    Entity a = new Entity("Person", "a");
    a.setProperty("lastName", "Johnson");
    Entity b = new Entity("Person", "b");
    b.setProperty("lastName", "Johnson");
    datastore.put(ImmutableList.<Entity>of(a, b));

    try {
      Entity result = retrievePersonWithLastName("Johnson");
      fail("Expected TooManyResultsException");
    } catch (TooManyResultsException expected) {
      // TooManyResultsException does not provide addition details.
    }
  }

  // [START gae_java8_datastore_query_limit]
  private List<Entity> getTallestPeople() {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query q = new Query("Person").addSort("height", SortDirection.DESCENDING);

    PreparedQuery pq = datastore.prepare(q);
    return pq.asList(FetchOptions.Builder.withLimit(5));
  }
  // [END gae_java8_datastore_query_limit]

  @Test
  public void queryLimitExample_returnsLimitedEntities() throws Exception {
    Entity a = new Entity("Person", "a");
    a.setProperty("height", 200);
    Entity b = new Entity("Person", "b");
    b.setProperty("height", 199);
    Entity c = new Entity("Person", "c");
    c.setProperty("height", 201);
    Entity d = new Entity("Person", "d");
    d.setProperty("height", 198);
    Entity e = new Entity("Person", "e");
    e.setProperty("height", 202);
    Entity f = new Entity("Person", "f");
    f.setProperty("height", 197);
    Entity g = new Entity("Person", "g");
    g.setProperty("height", 203);
    Entity h = new Entity("Person", "h");
    h.setProperty("height", 196);
    datastore.put(ImmutableList.<Entity>of(a, b, c, d, e, f, g, h));

    List<Entity> results = getTallestPeople();

    assertThat(results).named("results").containsExactly(g, e, c, a, b).inOrder();
  }
}
