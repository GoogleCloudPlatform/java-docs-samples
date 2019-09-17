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

import com.google.api.services.iam.v1.model.ServiceAccount;
import com.google.api.services.iam.v1.model.ServiceAccountKey;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class ServiceAccountTests {

  private ByteArrayOutputStream bout;

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");

  private static final String NAME = "java-test-" + new Random().nextInt(1000);

  private static final String EMAIL =  NAME + "@" + PROJECT_ID + ".iam.gserviceaccount.com";

  private String got;

  private static void requireEnvVar(String varName){
      assertNotNull(
        System.getenv(varName),
        String.format("Environment variable '%s' is required to perform these tests.", varName)
    );
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
  public void testServiceAccounts() throws Exception {
    
    //Testing ServiceAccountCreate
    ServiceAccount account = ServiceAccountCreate.createServiceAccount(PROJECT_ID, NAME, "Java Demo");
    assertTrue(account.getDisplayName().equals("Java Demo"));

    //Testing ServiceAccountList
    ServiceAccountsList.listServiceAccounts(PROJECT_ID);
    got = bout.toString();
    assertTrue(got.contains("Display Name: Java Demo"));

    //Testing ServiceAccountRename
    account = ServiceAccountRename.renameServiceAccount(EMAIL, "Java Demo (Updated!)");
    assertTrue(account.getDisplayName().equals("Java Demo (Updated!)"));

    //Testing ServiceAccountKeyCreate
    ServiceAccountKey key = ServiceAccountKeyCreate.createKey(EMAIL);
    got = bout.toString();
    assertTrue(got.contains("Created key:"));

    //Testing ServiceAccountKeyList
    ServiceAccountKeysList.listKeys(EMAIL);
    got = bout.toString();
    assertTrue(got.contains("Key:"));

    //Testing ServiceAccountKeyList
    ServiceAccountKeyDelete.deleteKey(key.getName());
    got = bout.toString();
    assertTrue(got.contains("Deleted key:"));

    //Testing ServiceAccountDelete
    ServiceAccountDelete.deleteServiceAccount(EMAIL);
    got = bout.toString();
    assertTrue(got.contains("Deleted service account:"));
  }
}
