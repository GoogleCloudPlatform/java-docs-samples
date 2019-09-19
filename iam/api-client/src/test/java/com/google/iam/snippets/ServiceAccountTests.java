/* Copyright 2018 Google LLC
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

package com.google.iam.snippets;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServiceAccountTests {

  private ByteArrayOutputStream bout;

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");

  private static void requireEnvVar(String varName) {
    assertNotNull(
        System.getenv(varName),
        String.format("Environment variable '%s' is required to perform these tests.", varName));
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @Before
  public void beforeTest() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @After
  public void tearDown() {
    System.setOut(null);
    bout.reset();
  }

  @Test
  public void stage1_testServiceAccountCreate() {
    CreateServiceAccount.createServiceAccount(PROJECT_ID);
    String got = bout.toString();
    assertTrue(got.contains("Created service account: your-service-account-name"));
  }

  @Test
  public void stage1_testServiceAccountsList() {
    ListServiceAccounts.listServiceAccounts(PROJECT_ID);
    String got = bout.toString();
    assertTrue(got.matches("(Name:.*\nDisplay Name:.*\nEmail.*\n\n)*"));
  }

  @Test
  public void stage2_testServiceAccountRename() {
    RenameServiceAccount.renameServiceAccount(PROJECT_ID);
    String got = bout.toString();
    assertTrue(got.contains("Updated display name"));
  }

  @Test
  public void stage2_testServiceAccountKeyCreate() {
    CreateServiceAccountKey.createKey(PROJECT_ID);
    String got = bout.toString();
    assertTrue(got.contains("Created key:"));
  }

  @Test
  public void stage2_testServiceAccountKeysList() {
    ListServiceAccountKeys.listKeys(PROJECT_ID);
    String got = bout.toString();
    assertTrue(got.contains("Key:"));
  }

  @Test
  public void stage3_testServiceAccountKeyDelete() {
    DeleteServiceAccountKey.deleteKey(PROJECT_ID);
    String got = bout.toString();
    assertTrue(got.contains("Deleted key:"));
  }

  /*
  @Test
  public void stage4_testServiceAccountDelete() {
    DeleteServiceAccount.deleteServiceAccount(PROJECT_ID);
    String got = bout.toString();
    assertTrue(got.contains("Deleted service account:"));
  }
  */
}