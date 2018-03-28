/*
 * Copyright 2018 Google Inc.
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
//CHECKSTYLE OFF: AbbreviationAsWordInName
public class AuthExampleIT {
  //CHECKSTYLE ON: AbbreviationAsWordInName
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
  public void testAuthImplicit() throws IOException {
    AuthExample.main(new String[] {});
    String output = bout.toString();
    assertTrue(output.contains("Buckets:"));
  }

  @Test
  public void testAuthExplicitNoPath() throws IOException {
    AuthExample.main(new String[] {"explicit", credentials});
    String output = bout.toString();
    assertTrue(output.contains("Buckets:"));
  }
}
