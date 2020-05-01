/* Copyright 2020 Google LLC
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
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.iam.v1.Iam;
import com.google.api.services.iam.v1.IamScopes;
import com.google.api.services.iam.v1.model.CreateServiceAccountRequest;
import com.google.api.services.iam.v1.model.ServiceAccount;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class QuickstartV2Tests {

  private ServiceAccount serviceAccount;
  private Iam iamService;
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

  // Creates a service account to use during the test
  @Before
  public void setUp() {
    try {
      GoogleCredential credential =
          GoogleCredential.getApplicationDefault()
              .createScoped(Collections.singleton(IamScopes.CLOUD_PLATFORM));

      iamService =
          new Iam.Builder(
                  GoogleNetHttpTransport.newTrustedTransport(),
                  JacksonFactory.getDefaultInstance(),
                  credential)
              .setApplicationName("service-accounts")
              .build();
    } catch (IOException | GeneralSecurityException e) {
      System.out.println("Unable to initialize service: \n" + e.toString());
      return;
    }

    try {
      serviceAccount = new ServiceAccount();
      serviceAccount.setDisplayName("iam-test-account" + new Date().hashCode());
      CreateServiceAccountRequest request = new CreateServiceAccountRequest();
      request.setAccountId("iam-test-account" + new Date().hashCode());
      request.setServiceAccount(serviceAccount);

      serviceAccount =
          iamService
              .projects()
              .serviceAccounts()
              .create("projects/" + PROJECT_ID, request)
              .execute();
    } catch (IOException e) {
      System.out.println("Unable to create service account: \n" + e.toString());
    }
  }

  // Deletes the service account used in the test.
  @After
  public void tearDown() {

    String resource = "projects/-/serviceAccounts/" + serviceAccount.getEmail();
    try {
      iamService.projects().serviceAccounts().delete(resource).execute();
    } catch (IOException e) {
      System.out.println("Unable to delete service account: \n" + e.toString());
    }
  }

  @Test
  public void testQuickstart() throws Exception {
    String member = "serviceAccount:" + serviceAccount.getEmail();
    String role = "roles/logging.logWriter";
    List<String> rolePermissions = Arrays.asList("logging.logEntries.create");

    // Tests initializeService()
    CloudResourceManager crmService = QuickstartV2.initializeService();

    // Tests addBinding()
    QuickstartV2.addBinding(crmService, PROJECT_ID, member, role);

    // Tests testPermissions()
    List<String> grantedPermissions =
        QuickstartV2.testPermissions(crmService, PROJECT_ID, member, rolePermissions);

    for (String p : rolePermissions) {
      assertTrue(grantedPermissions.contains(p));
    }

    // Tests removeMember()
    QuickstartV2.removeMember(crmService, PROJECT_ID, member, role);
  }
}
