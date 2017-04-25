/*
  Copyright 2016 Google Inc.

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

package com.example.bigquery;

import static com.google.common.truth.Truth.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/** Tests for simple app sample. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class QueryParametersSampleIT {
  private ByteArrayOutputStream bout;
  private PrintStream out;

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
  public void testNamedSample() throws Exception {
    QueryParametersSample.main(new String[] {"named", "romeoandjuliet", "100"});
    String got = bout.toString();
    assertThat(got).contains("love");
  }

  @Test
  public void testArraySample() throws Exception {
    QueryParametersSample.main(new String[] {"array", "M", "WA", "WI", "WV", "WY"});
    String got = bout.toString();
    assertThat(got).contains("James");
  }

  @Test
  public void testTimestampSample() throws Exception {
    QueryParametersSample.main(new String[] {"timestamp"});
    String got = bout.toString();
    assertThat(got).contains("2016-12-07T09:00:00Z");
  }
}
