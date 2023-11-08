/*
 * Copyright 2023 Google LLC
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

package com.google.cloud.auth.samples;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
//CHECKSTYLE OFF: AbbreviationAsWordInName
public class AccessTokenFromImpersonatedCredentialsIT {

  //CHECKSTYLE ON: AbbreviationAsWordInName
  private static final String impersonatedServiceAccount =
      System.getenv("IMPERSONATED_SERVICE_ACCOUNT");
  private static final String scope = "https://www.googleapis.com/auth/cloud-platform";
  private final PrintStream originalOut = System.out;
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private String credentials;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
    credentials = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
    assertNotNull(credentials);
  }

  @Test
  public void testAccessTokenFromImpersonatedCredentials()
      throws IOException {
    AccessTokenFromImpersonatedCredentials.getAccessToken(impersonatedServiceAccount, scope);
    String output = bout.toString();
    assertTrue(output.contains("Generated access token."));
  }

  @After
  public void tearDown() throws IOException {
    System.setOut(originalOut);
    bout.reset();
  }
}
