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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

// [START imports]
// [END imports]

// [START NameAndHelper]
public class LocalMemcacheTest {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalMemcacheServiceTestConfig());

  // [END NameAndHelper]

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  // Run this test twice to prove we're not leaking any state across tests.
  // [START doTest]
  private void doTest() {
    MemcacheService ms = MemcacheServiceFactory.getMemcacheService();
    assertFalse(ms.contains("yar"));
    ms.put("yar", "foo");
    assertTrue(ms.contains("yar"));
  }
  // [END doTest]

  @Test
  public void testInsert1() {
    doTest();
  }

  @Test
  public void testInsert2() {
    doTest();
  }
}
