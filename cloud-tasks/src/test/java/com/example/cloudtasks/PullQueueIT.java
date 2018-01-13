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

package com.example.cloudtasks;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Integration (system) tests for {@link PullQueue}.
 */
@RunWith(JUnit4.class)
//CHECKSTYLE OFF: AbbreviationAsWordInName
public class PullQueueIT {
  //CHECKSTYLE ON: AbbreviationAsWordInName

  private static final String PROJECT_ID = "java-docs-samples-cloud-tasks";
  private static final String LOCATION_ID = "us-central1";
  private static final String QUEUE_ID = "test-queue";

  private ByteArrayOutputStream bout;
  private PrintStream out;

  /**
   * Capture standard out for each test.
   */
  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);

    assertNotNull(PROJECT_ID);
  }

  /**
   * Reset standard out after each test.
   */
  @After
  public void tearDown() {
    System.setOut(null);
    bout.reset();
  }

  @Test
  public void testRunner() throws Exception {
    // Used to guarantee a specific test order.
    testCreateTask();
    testPullAndAckTask();
  }

  /**
   * Tests the the 'create-task' command.
   */
  private void testCreateTask() throws Exception {
    PullQueue.main(new String[] {
        "create-task",
        "--project", PROJECT_ID,
        "--location", LOCATION_ID,
        "--queue", QUEUE_ID
    });
    String output = bout.toString();
    assertTrue(output.contains("Created task"));
  }

  /**
   * Tests the the 'pull-and-ack-task' command.
   */
  private void testPullAndAckTask() throws Exception {
    PullQueue.main(new String[] {
        "pull-and-ack-task",
        "--project", PROJECT_ID,
        "--location", LOCATION_ID,
        "--queue", QUEUE_ID
    });
    String output = bout.toString();
    assertTrue(output.contains("Created task"));
  }

}
