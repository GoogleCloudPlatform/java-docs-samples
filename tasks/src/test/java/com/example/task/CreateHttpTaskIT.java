/*
 * Copyright 2019 Google LLC
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

package com.example.task;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for creating Tasks with HTTP targets. */
@RunWith(JUnit4.class)
public class CreateHttpTaskIT {

  private ByteArrayOutputStream bout;
  private PrintStream out;
  // @Rule public Timeout globalTimeout = Timeout.seconds(300); // 5 minute timeout

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() {
    System.setOut(null);
  }

  @Test
  public void testCreateHttpTask() throws Exception {
    CreateHttpTask.main();
    String got = bout.toString();
    assertThat(got).contains("Task created:");
  }

  @Test
  public void testCreateHttpTaskWithToken() throws Exception {
    String[] args = {" tasks-test@java-docs-samples-tests.iam.gserviceaccount.com"};
    CreateHttpTaskWithToken.main(args);
    String got = bout.toString();
    assertThat(got).contains("Task created:");
  }
}
