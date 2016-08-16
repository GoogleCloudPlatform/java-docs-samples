/*
 * Copyright (c) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not  use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.bigquery.samples.test;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.bigquery.samples.ListDatasetsProjects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.Exception;

/**
 * Unit tests for {@link ListDatasetsProjects}.
 */
@RunWith(JUnit4.class)
public class ListDatasetsProjectsTest {
  private static final PrintStream REAL_OUT = System.out;
  private static final PrintStream REAL_ERR = System.err;

  private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
  private final ByteArrayOutputStream stderr = new ByteArrayOutputStream();

  @Before
  public void setUp() {
    System.setOut(new PrintStream(stdout));
    System.setErr(new PrintStream(stderr));
  }

  @After
  public void tearDown() {
    System.setOut(REAL_OUT);
    System.setErr(REAL_ERR);
  }

  @Test
  public void testUsage() throws Exception {
    ListDatasetsProjects.main(new String[] {});
    assertThat(stderr.toString()).named("stderr").isEqualTo("Usage: QuickStart <project-id>\n");
  }

  @Test
  public void testMain() throws Exception {
    ListDatasetsProjects.main(new String[] {Constants.PROJECT_ID});
    String out = stdout.toString();
    assertThat(out).named("stdout").contains("Running the asynchronous query");
    assertThat(out).named("stdout").containsMatch("George W. Bush, [0-9]+");
    assertThat(out).named("stdout").containsMatch("Wikipedia, [0-9]+");

    assertThat(out).named("stdout").contains("Listing all the Datasets");
    assertThat(out).named("stdout").contains("test_dataset");

    assertThat(out).named("stdout").contains("Listing all the Projects");
    assertThat(out).named("stdout").contains("Project list:");
    assertThat(out).named("stdout").containsMatch("Bigquery Samples|cloud-samples-tests");
  }
}
