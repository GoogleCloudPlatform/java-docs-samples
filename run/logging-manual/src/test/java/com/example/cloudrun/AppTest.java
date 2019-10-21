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

package com.example.cloudrun;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static spark.Spark.awaitInitialization;
import static spark.Spark.stop;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import spark.utils.IOUtils;

public class AppTest {

  private ByteArrayOutputStream bout;
  private PrintStream out;

  @BeforeClass
  public static void beforeClass() {
    App app = new App();
    app.main(new String[] {});
    awaitInitialization();
  }

  @AfterClass
  public static void afterClass() {
    stop();
  }

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @Test
  public void shouldSucceed() throws IOException {
    TestResponse response = executeRequest("GET", "/");
    assertEquals(200, response.status);
    assertEquals("Hello Logger!", response.body);
    String output = bout.toString();
    assertTrue(output.toString().contains("This is the default display field."));
    assertTrue(output.toString().contains("NOTICE"));
    assertTrue(output.toString().contains("arbitrary-property"));
  }

  private static TestResponse executeRequest(String method, String path) throws IOException {
    URL url = new URL("http://localhost:8080" + path);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod(method);
    connection.setDoOutput(true);
    connection.connect();
    String body = IOUtils.toString(connection.getInputStream());
    return new TestResponse(connection.getResponseCode(), body);
  }

  public static class TestResponse {

    public final String body;
    public final int status;

    public TestResponse(int status, String body) {
      this.status = status;
      this.body = body;
    }
  }
}
