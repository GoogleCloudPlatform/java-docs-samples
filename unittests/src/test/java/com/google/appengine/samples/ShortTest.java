/*
 * Copyright 2015 Google Inc.
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

// [START ShortTest]

import static org.junit.Assert.assertEquals;

import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityStatus;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalCapabilitiesServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.apphosting.api.ApiProxy;
import org.junit.After;
import org.junit.Test;

public class ShortTest {
  private LocalServiceTestHelper helper;

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test(expected = ApiProxy.CapabilityDisabledException.class)
  public void testDisabledDatastore() {
    Capability testOne = new Capability("datastore_v3");
    CapabilityStatus testStatus = CapabilityStatus.DISABLED;
    // Initialize the test configuration.
    LocalCapabilitiesServiceTestConfig config =
        new LocalCapabilitiesServiceTestConfig().setCapabilityStatus(testOne, testStatus);
    helper = new LocalServiceTestHelper(config);
    helper.setUp();
    FetchOptions fo = FetchOptions.Builder.withLimit(10);
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    assertEquals(0, ds.prepare(new Query("yam")).countEntities(fo));
  }
}

// [END ShortTest]
