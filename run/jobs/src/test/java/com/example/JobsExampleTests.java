/*
 * Copyright 2021 Google LLC
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

package com.example;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JobsExampleTests {

  private final PrintStream originalOut = System.out;
  private ByteArrayOutputStream output;

  @Before
  public void beforeEach() {
    output = new ByteArrayOutputStream();
    System.setOut(new PrintStream(output));
    System.setErr(new PrintStream(output));
  }

  @After
  public void afterEach() {
    System.setOut(originalOut);
    output.reset();
  }

  @Test
  public void handlesSuccess() throws InterruptedException {
    JobsExample.runTask(0, 0.0f);
    assertThat(output.toString()).contains("Completed Task #0");
  }

  @Test
  public void handlesFailure() throws InterruptedException {
    try {
      JobsExample.runTask(0, 0.999f);
    } catch (RuntimeException err) {
      assertThat(err.getMessage()).contains("Task Failed.");
    }
  }

  @Test
  public void runsMain() {
    JobsExample.main(null);
    assertThat(output.toString()).contains("Completed Task #0");
  }
}
