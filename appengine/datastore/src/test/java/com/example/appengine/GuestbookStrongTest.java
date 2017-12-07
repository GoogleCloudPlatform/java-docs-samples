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
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import java.util.List;
import org.joda.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link GuestbookStrong}.
 */
@RunWith(JUnit4.class)
public class GuestbookStrongTest {
  private static final Instant FAKE_NOW = new Instant(1234567890L);
  private static final String GUESTBOOK_ID = "my guestbook";

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          // Set maximum eventual consistency.
          // https://cloud.google.com/appengine/docs/java/tools/localunittesting#Java_Writing_High_Replication_Datastore_tests
          new LocalDatastoreServiceTestConfig()
              .setDefaultHighRepJobPolicyUnappliedJobPercentage(100),
          // Make sure there is a user logged in. We enforce this in web.xml.
          new LocalUserServiceTestConfig())
      .setEnvIsLoggedIn(true)
      .setEnvEmail("test@example.com")
      .setEnvAuthDomain("gmail.com");

  private FakeClock clock;
  private GuestbookStrong guestbookUnderTest;

  @Before
  public void setUp() throws Exception {
    helper.setUp();
    clock = new FakeClock(FAKE_NOW);
    guestbookUnderTest = new GuestbookStrong(GUESTBOOK_ID, clock);
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
  public void appendGreeting_normalData_setsDateProperty() {
    Greeting got = guestbookUnderTest.appendGreeting("Hello, Datastore!");

    assertThat(got.getDate())
        .named("date property")
        .isEqualTo(FAKE_NOW);
  }

  @Test
  public void listGreetings_maximumEventualConsistency_returnsAllGreetings() {
    // Arrange
    guestbookUnderTest.appendGreeting("Hello, Datastore!");
    guestbookUnderTest.appendGreeting("Hello, Eventual Consistency!");
    guestbookUnderTest.appendGreeting("Hello, World!");

    // Act
    List<Greeting> got = guestbookUnderTest.listGreetings();

    // Assert
    // Since we use an ancestor query, all greetings should be available.
    assertThat(got).hasSize(3);
  }
}

