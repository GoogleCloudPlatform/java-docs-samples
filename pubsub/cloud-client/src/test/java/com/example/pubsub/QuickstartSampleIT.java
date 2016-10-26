/*
  Copyright 2016, Google, Inc.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package com.example.pubsub;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.pubsub.PubSub;
import com.google.cloud.pubsub.PubSubOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Tests for quickstart sample.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class QuickstartSampleIT {
  private ByteArrayOutputStream bout;
  private PrintStream out;

  private static final void deleteTestTopic() {
    PubSub pubsub = PubSubOptions.defaultInstance().service();
    String topicName = "my-new-topic";
    pubsub.deleteTopic(topicName);
  }

  @Before
  public void setUp() {
    deleteTestTopic();

    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() {
    System.setOut(null);
    deleteTestTopic();
  }

  @Test
  public void testQuickstart() throws Exception {
    QuickstartSample.main();
    String got = bout.toString();
    assertThat(got).contains("Topic my-new-topic created.");
  }
}
// [END datastore_quickstart]
