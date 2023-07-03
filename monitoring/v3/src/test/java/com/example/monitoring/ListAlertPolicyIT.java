/*
 * Copyright 2020 Google LLC
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

package com.example.monitoring;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertNotNull;

import com.google.monitoring.v3.AlertPolicy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Tests for list an alert policy sample. */
public class ListAlertPolicyIT {
  private static final String PROJECT_ID = requireEnvVar();
  private ByteArrayOutputStream bout;
  private PrintStream originalPrintStream;
  private static String policyName;
  private static final String suffix = UUID.randomUUID().toString().substring(0, 8);
  private static final String testPolicyName = "test-policy" + suffix;

  private static String requireEnvVar() {
    String value = System.getenv("GOOGLE_CLOUD_PROJECT");
    assertNotNull(
        "Environment variable " + "GOOGLE_CLOUD_PROJECT" + " is required to perform these tests.",
        System.getenv("GOOGLE_CLOUD_PROJECT"));
    return value;
  }

  @BeforeClass
  public static void checkRequirements() throws IOException {
    requireEnvVar();
    AlertPolicy policy = CreateAlertPolicy.createAlertPolicy(PROJECT_ID, testPolicyName);
    policyName = policy.getName();
  }

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);
    originalPrintStream = System.out;
    System.setOut(out);
  }

  @After
  public void tearDown() {
    // restores print statements in the original method
    System.out.flush();
    System.setOut(originalPrintStream);
  }

  @AfterClass
  public static void tearDownClass() throws IOException {
    DeleteAlertPolicy.deleteAlertPolicy(policyName);
  }

  @Test
  public void listAlertPolicyTest() throws IOException {
    ListAlertPolicy.listAlertPolicy(PROJECT_ID);
    assertThat(bout.toString()).contains("success! alert policy");
  }
}
