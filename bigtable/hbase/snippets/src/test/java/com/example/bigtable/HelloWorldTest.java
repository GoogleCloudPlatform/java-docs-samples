/*
 * Copyright 2022 Google LLC
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

package com.example.bigtable;

import static org.junit.Assert.assertNotNull;

import com.google.common.truth.Truth;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class HelloWorldTest {
  private static String projectId;
  private static String instanceId;
  private ByteArrayOutputStream bout;

  @BeforeClass
  public static void beforeClass() throws IOException {
    projectId = requireEnv("GOOGLE_CLOUD_PROJECT");
    instanceId = requireEnv("BIGTABLE_TESTING_INSTANCE");
  }

  @Before
  public void setupStream() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @Test
  public void helloWorld() {
    HelloWorld.doHelloWorld(projectId, instanceId);

    Truth.assertThat(bout.toString()).contains("HelloWorld: Create table Hello-Bigtable");
    Truth.assertThat(bout.toString()).contains("HelloWorld: Write some greetings to the table");
    Truth.assertThat(bout.toString()).contains("Get a single greeting by row key");
    Truth.assertThat(bout.toString()).contains("greeting0 = Hello World!");
    Truth.assertThat(bout.toString()).contains("HelloWorld: Scan for all greetings:");
    Truth.assertThat(bout.toString()).contains("Hello World!");
    Truth.assertThat(bout.toString()).contains("Hello Cloud Bigtable!");
    Truth.assertThat(bout.toString()).contains("Hello HBase!");
    Truth.assertThat(bout.toString()).contains("HelloWorld: Delete the table");
  }

  private static String requireEnv(String varName) {
    String value = System.getenv(varName);
    assertNotNull(
        String.format("Environment variable '%s' is required to perform these tests.", varName),
        value);
    return value;
  }
}
