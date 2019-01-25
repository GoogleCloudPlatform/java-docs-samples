/*
 * Copyright 2017 Google Inc.
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

package com.example.firestore.snippets;

import com.example.firestore.snippets.model.City;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.Query.Direction;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

import com.google.firestore.v1beta1.Document;
import com.google.protobuf.Api;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Snippets to support firestore querying data documentation. */
class QueryDataSnippets {

  private final Firestore db;

  QueryDataSnippets(Firestore db) {
    this.db = db;
  }

  /**
   * Creates cities collection and add sample documents to test queries.
   *
   * @return collection reference
   */
  void prepareExamples() throws Exception {

    // [START fs_query_create_examples]
    CollectionReference cities = db.collection("cities");
    List<ApiFuture<WriteResult>> futures = new ArrayList<>();
    futures.add(cities.document("SF").set(new City("San Francisco", "CA", "USA", false, 860000L,
        Arrays.asList("west_coast", "norcal"))));
    futures.add(cities.document("LA").set(new City("Los Angeles", "CA", "USA", false, 3900000L,
        Arrays.asList("west_coast", "socal"))));
    futures.add(cities.document("DC").set(new City("Washington D.C.", null, "USA", true, 680000L,
        Arrays.asList("east_coast"))));
    futures.add(cities.document("TOK").set(new City("Tokyo", null, "Japan", true, 9000000L,
        Arrays.asList("kanto", "honshu"))));
    futures.add(cities.document("BJ").set(new City("Beijing", null, "China", true, 21500000L,
        Arrays.asList("jingjinji", "hebei"))));
    // (optional) block on documents successfully added
    ApiFutures.allAsList(futures).get();
    // [END fs_query_create_examples]
  }

  /**
   * Creates a sample query.
   *
   * @return query
   */
  Query createAQuery() throws Exception {
    // [START fs_create_query]
    // Create a reference to the cities collection
    CollectionReference cities = db.collection("cities");
    // Create a query against the collection.
    Query query = cities.whereEqualTo("capital", true);
    // retrieve  query results asynchronously using query.get()
    ApiFuture<QuerySnapshot> querySnapshot = query.get();

    for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
      System.out.println(document.getId());
    }
    // [END fs_create_query]
    return query;
  }

  /**
   * Creates a sample query.
   *
   * @return query
   */
  Query createAQueryAlternate() throws Exception {
    // [START fs_create_query_country]
    // Create a reference to the cities collection
    CollectionReference cities = db.collection("cities");
    // Create a query against the collection.
    Query query = cities.whereEqualTo("state", "CA");
    // retrieve  query results asynchronously using query.get()
    ApiFuture<QuerySnapshot> querySnapshot = query.get();

    for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
      System.out.println(document.getId());
    }
    // [END fs_create_query_country]
    return query;
  }

  /**
   * Creates queries with simple where clauses.
   *
   * @return queries
   */
  List<Query> createSimpleQueries() {
    List<Query> querys = new ArrayList<>();
    CollectionReference cities = db.collection("cities");

    // [START fs_simple_queries]
    Query stateQuery = cities.whereEqualTo("state", "CA");
    Query populationQuery = cities.whereLessThan("population", 1000000L);
    Query nameQuery = cities.whereGreaterThanOrEqualTo("name", "San Francisco");
    // [END fs_simple_queries]

    querys.add(stateQuery);
    querys.add(populationQuery);
    querys.add(nameQuery);
    return querys;
  }

  /**
   * Creates a query based on array containment.
   *
   * @return query
   */
  Query createArrayQuery() {
    // [START fs_array_contains_filter]
    CollectionReference citiesRef = db.collection("cities");
    Query westCoastQuery = citiesRef.whereArrayContains("regions", "west_coast");
    // [END fs_array_contains_filter]

    return westCoastQuery;
  }

  /**
   * Creates chained where clauses.
   *
   * <p>Note : equality and inequality clauses over multiple fields cannot be chained.
   *
   * @return query
   */
  Query createChainedQuery() {
    CollectionReference cities = db.collection("cities");
    // [START fs_chained_query]
    Query chainedQuery1 = cities.whereEqualTo("state", "CO")
        .whereEqualTo("name", "Denver");
    // [END fs_chained_query]
    return chainedQuery1;
  }

  /**
   * An instance of a currently unsupported chained query: equality with inequality.
   * NOTE : Requires support for creation of composite indices.
   *
   * @return query
   */
  Query createCompositeIndexChainedQuery() {
    CollectionReference cities = db.collection("cities");
    // [START fs_composite_index_chained_query]
    Query chainedQuery2 = cities.whereEqualTo("state", "CA")
        .whereLessThan("population", 1000000L);
    // [END fs_composite_index_chained_query]
    return chainedQuery2;
  }

  /**
   * An instance of a valid range/inequality query : range operators are limited to a single field.
   *
   * @return query
   */
  Query createRangeQuery() {
    CollectionReference cities = db.collection("cities");
    // [START fs_range_query]
    Query validQuery1 = cities.whereGreaterThanOrEqualTo("state", "CA")
        .whereLessThanOrEqualTo("state", "IN");
    Query validQuery2 = cities.whereEqualTo("state", "CA")
        .whereGreaterThan("population", 1000000);
    // [END fs_range_query]
    return validQuery1;
  }

  /**
   * An instance of an invalid range query : range operators are limited to a single field.
   *
   * @return query
   */
  Query createInvalidRangeQuery() {
    CollectionReference cities = db.collection("cities");
    // Violates constraint : range operators are limited to a single field
    // [START fs_invalid_range_query]
    Query invalidRangeQuery = cities.whereGreaterThanOrEqualTo("state", "CA")
        .whereGreaterThan("population", 100000);
    // [END fs_invalid_range_query]
    return invalidRangeQuery;
  }

  /**
   * Creates a query that combines order by with limit.
   *
   * @return query
   */
  Query createOrderByNameWithLimitQuery() {
    CollectionReference cities = db.collection("cities");
    // [START fs_order_by_name_limit_query]
    Query query = cities.orderBy("name").limit(3);
    // [END fs_order_by_name_limit_query]
    return query;
  }

  /**
   * Creates a query that orders by country and population(descending).
   *
   * @return query
   */
  Query createOrderByCountryAndPopulation() {
    CollectionReference cities = db.collection("cities");
    // [START fs_order_by_country_population]
    Query query = cities.orderBy("state").orderBy("population", Direction.DESCENDING);
    // [END fs_order_by_country_population]
    return query;
  }

  /**
   * Creates a query that combines order by in descending order with the limit operator.
   *
   * @return query
   */
  Query createOrderByNameDescWithLimitQuery() {
    CollectionReference cities = db.collection("cities");
    // [START fs_order_by_name_desc_limit_query]
    Query query = cities.orderBy("name", Direction.DESCENDING).limit(3);
    // [END fs_order_by_name_desc_limit_query]
    return query;
  }

  /**
   * Creates a query that combines where clause with order by and limit operator.
   *
   * @return query
   */
  Query createWhereWithOrderByAndLimitQuery() {
    CollectionReference cities = db.collection("cities");
    // [START fs_where_order_by_limit_query]
    Query query = cities.whereGreaterThan("population", 2500000L).orderBy("population").limit(2);
    // [END fs_where_order_by_limit_query]
    return query;
  }

  /**
   * Creates a query using a range where clause with order by. Order by must be based on the same
   * field as the range clause.
   *
   * @return query
   */
  Query createRangeWithOrderByQuery() {
    CollectionReference cities = db.collection("cities");
    // [START fs_range_order_by_query]
    Query query = cities.whereGreaterThan("population", 2500000L).orderBy("population");
    // [END fs_range_order_by_query]
    return query;
  }

  /**
   * Creates an instance of an invalid range combined with order. Violates the constraint that range
   * and order by are required to be on the same field.
   *
   * @return query
   */
  Query createInvalidRangeWithOrderByQuery() {
    CollectionReference cities = db.collection("cities");
    // Violates the constraint that range and order by are required to be on the same field
    // [START fs_invalid_range_order_by_query]
    Query query = cities.whereGreaterThan("population", 2500000L).orderBy("country");
    // [END fs_invalid_range_order_by_query]
    return query;
  }

  /**
   * Create a query defining the start point of a query.
   *
   * @return query
   */
  Query createStartAtFieldQueryCursor() {
    CollectionReference cities = db.collection("cities");
    // [START fs_start_at_field_query_cursor]
    Query query = cities.orderBy("population").startAt(4921000L);
    // [END fs_start_at_field_query_cursor]
    return query;
  }

  /**
   * Create a query defining the start point of a query.
   *
   * @return query
   */
  Query createEndAtFieldQueryCursor() {
    CollectionReference cities = db.collection("cities");
    // [START fs_end_at_field_query_cursor]
    Query query = cities.orderBy("population").endAt(4921000L);
    // [END fs_end_at_field_query_cursor]
    return query;
  }

  /* Create queries with multiple cursor conditions. */
  void createMultipleCursorConditionsQuery() {
    // [START fs_multiple_cursor_conditions]
    // Will return all Springfields
    Query query1 = db.collection("cities")
        .orderBy("name")
        .orderBy("state")
        .startAt("Springfield");

    // Will return "Springfield, Missouri" and "Springfield, Wisconsin"
    Query query2 = db.collection("cities")
        .orderBy("name")
        .orderBy("state")
        .startAt("Springfield", "Missouri");
    // [END fs_multiple_cursor_conditions]
  }

  /**
   * Create a query using a snapshot as a start point.
   *
   * @return query
   */
  Query createStartAtSnapshotQueryCursor()
      throws InterruptedException, ExecutionException, TimeoutException {
    // [START fs_document_snapshot_cursor]
    // Fetch the snapshot with an API call, waiting for a maximum of 30 seconds for a result.
    ApiFuture<DocumentSnapshot> future = db.collection("cities").document("SF").get();
    DocumentSnapshot snapshot = future.get(30, TimeUnit.SECONDS);

    // Construct the query
    Query query = db.collection("cities")
        .orderBy("population")
        .startAt(snapshot);
    // [END fs_document_snapshot_cursor]
    return query;
  }

  /**
   * Example of a paginated query.
   */
  List<Query> paginateCursor() throws InterruptedException, ExecutionException, TimeoutException {
    // [START fs_paginate_cursor]
    // Construct query for first 25 cities, ordered by population.
    CollectionReference cities = db.collection("cities");
    Query firstPage = cities
        .orderBy("population")
        .limit(25);

    // Wait for the results of the API call, waiting for a maximum of 30 seconds for a result.
    ApiFuture<QuerySnapshot> future = firstPage.get();
    List<QueryDocumentSnapshot> docs = future.get(30, TimeUnit.SECONDS).getDocuments();

    // Construct query for the next 25 cities.
    QueryDocumentSnapshot lastDoc = docs.get(docs.size() - 1);
    Query secondPage = cities
        .orderBy("population")
        .startAfter(lastDoc)
        .limit(25);

    future = secondPage.get();
    docs = future.get(30, TimeUnit.SECONDS).getDocuments();
    // [END fs_paginate_cursor]
    return Arrays.asList(firstPage, secondPage);
  }
}
