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

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.iam.v1.model.ServiceAccount;
import com.google.api.services.iam.v1.model.ServiceAccountKey;
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
    try {
      CreateServiceAccount.createServiceAccount(PROJECT_ID);
      String got = bout.toString();
      assertTrue(got.contains("Created service account: service-account-name"));
    } catch (Exception e) {
      System.out.println("Unable to create service account: /n" + e.toString());
    }
  }

  @Test
  public void stage1_testServiceAccountsList() {
    try {
      ListServiceAccounts.listServiceAccounts(PROJECT_ID);
      String got = bout.toString();
      assertTrue(got.matches("(Name:.*\nDisplay Name:.*\nEmail.*\n\n)*"));
    } catch (Exception e) {
      System.out.println("Unable to list service accounts: /n" + e.toString());
    }
  }

  @Test
  public void stage2_testServiceAccountRename() {
    try {
      RenameServiceAccount.renameServiceAccount(PROJECT_ID);
      String got = bout.toString();
      assertTrue(got.contains("Updated display name"));
    } catch (Exception e) {
      System.out.println("Unable to rename service account: /n" + e.toString());
    }
  }

  @Test
  public void stage2_testServiceAccountKeyCreate() {
    try {
      CreateServiceAccountKey.createKey(PROJECT_ID);
      String got = bout.toString();
      assertTrue(got.contains("Created key:"));
    } catch (Exception e) {
      System.out.println("Unable to create service account key: /n" + e.toString());
    }
  }

  @Test
  public void stage2_testServiceAccountKeysList() {
    try {
      ListServiceAccountKeys.listKeys(PROJECT_ID);
      String got = bout.toString();
      assertTrue(got.contains("Key:"));
    } catch (Exception e) {
      System.out.println("Unable to list service account keys: /n" + e.toString());
    }
  }

  @Test
  public void stage3_testServiceAccountKeyDelete() {
    try {
      DeleteServiceAccountKey.deleteKey(PROJECT_ID);
      String got = bout.toString();
      assertTrue(got.contains("Deleted key:"));
    } catch (Exception e) {
      System.out.println("Unable to delete service account key: /n" + e.toString());
    }
  }

  @Test
  public void stage3_testServiceAccountDelete() {
    try {
      DeleteServiceAccount.deleteServiceAccount(PROJECT_ID);
      String got = bout.toString();
      assertTrue(got.contains("Deleted service account:"));
    } catch (Exception e) {
      System.out.println("Unable to delete service account: /n" + e.toString());
    }
  }
}
