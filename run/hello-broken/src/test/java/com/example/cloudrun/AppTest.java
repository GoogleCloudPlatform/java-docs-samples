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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import spark.utils.IOUtils;

public class AppTest {

  private static final String brokenUrl = "/";
  private static final String fixedUrl = "/improved";

  @Rule public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

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

  @Test
  public void brokenShouldFailOnAnyRequest() {
    try {
      TestResponse response = executeRequest("GET", brokenUrl);
      assertEquals(true, response);
    } catch (IOException e) {
      assertTrue(e.getMessage().startsWith("Server returned HTTP response code: 500"));
    }
  }

  @Test
  public void fixShouldSucceedWithDefault() throws IOException {
    TestResponse response = executeRequest("GET", fixedUrl);
    assertEquals(200, response.status);
    assertEquals("Hello World!", response.body);
  }

  @Test
  public void brokenShouldSucceedWithName() throws IOException {
    environmentVariables.set("NAME", "test");
    TestResponse response = executeRequest("GET", brokenUrl);
    assertEquals(200, response.status);
    assertEquals("Hello test!", response.body);
  }

  @Test
  public void fixShouldSucceedWithName() throws IOException {
    environmentVariables.set("NAME", "test");
    TestResponse response = executeRequest("GET", fixedUrl);
    assertEquals(200, response.status);
    assertEquals("Hello test!", response.body);
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
