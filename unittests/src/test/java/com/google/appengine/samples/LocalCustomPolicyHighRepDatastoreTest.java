/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.google.appengine.samples;

// [START LocalCustomPolicyHighRepDatastoreTest]

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static org.junit.Assert.assertEquals;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.dev.HighRepJobPolicy;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LocalCustomPolicyHighRepDatastoreTest {
  private static final class CustomHighRepJobPolicy implements HighRepJobPolicy {
    static int newJobCounter = 0;
    static int existingJobCounter = 0;

    @Override
    public boolean shouldApplyNewJob(Key entityGroup) {
      // Every other new job fails to apply.
      return newJobCounter++ % 2 == 0;
    }

    @Override
    public boolean shouldRollForwardExistingJob(Key entityGroup) {
      // Every other existing job fails to apply.
      return existingJobCounter++ % 2 == 0;
    }
  }

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
          .setAlternateHighRepJobPolicyClass(CustomHighRepJobPolicy.class));

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testEventuallyConsistentGlobalQueryResult() {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    ds.put(new Entity("yam")); // applies
    ds.put(new Entity("yam")); // does not apply
    // First global query only sees the first Entity.
    assertEquals(1, ds.prepare(new Query("yam")).countEntities(withLimit(10)));
    // Second global query sees both Entities because we "groom" (attempt to
    // apply unapplied jobs) after every query.
    assertEquals(2, ds.prepare(new Query("yam")).countEntities(withLimit(10)));
  }
}
// [END LocalCustomPolicyHighRepDatastoreTest]
