/**
 * Copyright 2015 Google Inc. All Rights Reserved.
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
import static com.jcabi.matchers.RegexMatchers.containsPattern;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Tests the Cloud Logging sample.
 */
public class ListLogsTest {
  private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
  private final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
  private static final PrintStream REAL_OUT = System.out;
  private static final PrintStream REAL_ERR = System.err;

  @Before
  public void setUp() {
    System.setOut(new PrintStream(stdout));
    System.setErr(new PrintStream(stderr));
  }

  @After
  public void tearDown() {
    System.setOut(ListLogsTest.REAL_OUT);
    System.setErr(ListLogsTest.REAL_ERR);
  }

  @Test
  public void testUsage() throws Exception {
    ListLogs.main(new String[] {});
    assertEquals("Usage: ListLogs <project-name>\n", stderr.toString());
  }

  @Test
  public void testListLogs() throws Exception {
    ListLogs.main(new String[] {"cloud-samples-tests"});
    String out = stdout.toString();
    // Don't know what logs the test project will have.
    assertThat(out, containsPattern("Done\\."));
  }
}
