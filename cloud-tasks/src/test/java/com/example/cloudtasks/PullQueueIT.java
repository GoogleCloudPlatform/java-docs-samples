/**
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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Integration (system) tests for {@link PullQueue}.
 */
@RunWith(JUnit4.class)
public class PullQueueIT {
  private ByteArrayOutputStream bout;
  private PrintStream out;

  static final private String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  static final private String LOCATION_ID = "us-central1";
  static final private String QUEUE_ID = "test-queue";

  /**
   * Capture standard out for each test.
   */
  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);

    assertNotNull(System.getenv("GOOGLE_CLOUD_PROJECT"));
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
