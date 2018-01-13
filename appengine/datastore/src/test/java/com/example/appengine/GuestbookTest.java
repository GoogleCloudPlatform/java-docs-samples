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

import com.example.time.testing.FakeClock;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.dev.HighRepJobPolicy;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link Guestbook}.
 */
@RunWith(JUnit4.class)
public class GuestbookTest {
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
      // Existing jobs always apply after every Get and every Query.
      return true;
    }
  }

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          // Set custom, deterministic, eventual consistency.
          // https://cloud.google.com/appengine/docs/java/tools/localunittesting#Java_Writing_High_Replication_Datastore_tests
          new LocalDatastoreServiceTestConfig()
              .setAlternateHighRepJobPolicyClass(CustomHighRepJobPolicy.class),
          // Make sure there is a user logged in. We enforce this in web.xml.
          new LocalUserServiceTestConfig())
      .setEnvIsLoggedIn(true)
      .setEnvEmail("test@example.com")
      .setEnvAuthDomain("gmail.com");

  private FakeClock clock;
  private Guestbook guestbookUnderTest;

  @Before
  public void setUp() throws Exception {
    helper.setUp();
    clock = new FakeClock();
    guestbookUnderTest = new Guestbook(clock);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void appendGreeting_normalData_setsContentProperty() {
    Greeting got = guestbookUnderTest.appendGreeting("Hello, Datastore!");

    assertThat(got.getContent())
        .named("content property")
        .isEqualTo("Hello, Datastore!");
  }

  @Test
  public void listGreetings_eventualConsistency_returnsPartialGreetings() {
    // Arrange
    guestbookUnderTest.appendGreeting("Hello, Datastore!");
    guestbookUnderTest.appendGreeting("Hello, Eventual Consistency!");
    guestbookUnderTest.appendGreeting("Hello, World!");
    guestbookUnderTest.appendGreeting("Güten Tag!");

    // Act
    List<Greeting> got = guestbookUnderTest.listGreetings();

    // The first time we query we should half of the results due to the fact that we simulate
    // eventual consistency by applying every other write.
    assertThat(got).hasSize(2);
  }

  @Test
  public void listGreetings_groomedDatastore_returnsAllGreetings() {
    // Arrange
    guestbookUnderTest.appendGreeting("Hello, Datastore!");
    guestbookUnderTest.appendGreeting("Hello, Eventual Consistency!");
    guestbookUnderTest.appendGreeting("Hello, World!");

    // Act
    guestbookUnderTest.listGreetings();
    // Second global query sees both Entities because we "groom" (attempt to
    // apply unapplied jobs) after every query.
    List<Greeting> got = guestbookUnderTest.listGreetings();

    assertThat(got).hasSize(3);
  }
}
