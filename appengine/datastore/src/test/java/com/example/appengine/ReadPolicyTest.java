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
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.ReadPolicy;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link ReadPolicy}.
 */
@RunWith(JUnit4.class)
public class ReadPolicyTest {
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          // Set 100% eventual consistency, so we can test with other job policies.
          // https://cloud.google.com/appengine/docs/java/tools/localunittesting#Java_Writing_High_Replication_Datastore_tests
          new LocalDatastoreServiceTestConfig()
              .setDefaultHighRepJobPolicyUnappliedJobPercentage(100));

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void readPolicy_eventual_returnsNoResults() {
    // [START data_consistency]
    double deadline = 5.0;

    // Construct a read policy for eventual consistency
    ReadPolicy policy = new ReadPolicy(ReadPolicy.Consistency.EVENTUAL);

    // Set the read policy
    DatastoreServiceConfig eventuallyConsistentConfig =
        DatastoreServiceConfig.Builder.withReadPolicy(policy);

    // Set the call deadline
    DatastoreServiceConfig deadlineConfig = DatastoreServiceConfig.Builder.withDeadline(deadline);

    // Set both the read policy and the call deadline
    DatastoreServiceConfig datastoreConfig =
        DatastoreServiceConfig.Builder.withReadPolicy(policy).deadline(deadline);

    // Get Datastore service with the given configuration
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService(datastoreConfig);
    // [END data_consistency]

    Entity parent = new Entity("Person", "a");
    Entity child = new Entity("Person", "b", parent.getKey());
    datastore.put(ImmutableList.<Entity>of(parent, child));

    // Even though we are using an ancestor query, the policy is set to
    // eventual, so we should get eventually-consistent results. Since the
    // local data store test config is set to 100% unapplied jobs, there
    // should be no results.
    Query q = new Query("Person").setAncestor(parent.getKey());
    List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    assertThat(results).named("query results").isEmpty();
  }

  @Test
  public void readPolicy_strong_returnsAllResults() {
    double deadline = 5.0;
    ReadPolicy policy = new ReadPolicy(ReadPolicy.Consistency.STRONG);
    DatastoreServiceConfig datastoreConfig =
        DatastoreServiceConfig.Builder.withReadPolicy(policy).deadline(deadline);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService(datastoreConfig);

    Entity parent = new Entity("Person", "a");
    Entity child = new Entity("Person", "b", parent.getKey());
    datastore.put(ImmutableList.<Entity>of(parent, child));

    Query q = new Query("Person").setAncestor(parent.getKey());
    List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
    assertThat(results).named("query results").hasSize(2);
  }
}
