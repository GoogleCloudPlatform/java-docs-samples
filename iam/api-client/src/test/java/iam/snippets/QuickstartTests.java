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

package iam.snippets;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertNotNull;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.model.Binding;
import com.google.api.services.cloudresourcemanager.model.Policy;
import com.google.api.services.iam.v1.Iam;
import com.google.api.services.iam.v1.IamScopes;
import com.google.api.services.iam.v1.model.CreateServiceAccountRequest;
import com.google.api.services.iam.v1.model.ServiceAccount;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class QuickstartTests {

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
      GoogleCredentials credential =
          GoogleCredentials.getApplicationDefault()
              .createScoped(Collections.singleton(IamScopes.CLOUD_PLATFORM));

      iamService =
          new Iam.Builder(
                  GoogleNetHttpTransport.newTrustedTransport(),
                  JacksonFactory.getDefaultInstance(),
                  new HttpCredentialsAdapter(credential))
              .setApplicationName("service-accounts")
              .build();
    } catch (IOException | GeneralSecurityException e) {
      System.out.println("Unable to initialize service: \n" + e.toString());
      return;
    }

    try {
      serviceAccount = new ServiceAccount();
      String serviceAccountUuid = UUID.randomUUID().toString().split("-")[0];
      serviceAccount.setDisplayName("iam-test-account" + serviceAccountUuid);
      CreateServiceAccountRequest request = new CreateServiceAccountRequest();
      request.setAccountId("iam-test-account" + serviceAccountUuid);
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

    // Tests initializeService()
    CloudResourceManager crmService = Quickstart.initializeService();

    // Tests addBinding()
    Quickstart.addBinding(crmService, PROJECT_ID, member, role);

    // Get the project's polcy and confirm that the member is in the policy
    Policy policy = Quickstart.getPolicy(crmService, PROJECT_ID);
    Binding binding = null;
    List<Binding> bindings = policy.getBindings();
    for (Binding b : bindings) {
      if (b.getRole().equals(role)) {
        binding = b;
        break;
      }
    }
    assertThat(binding.getMembers(), hasItem(member));

    // Tests removeMember()
    Quickstart.removeMember(crmService, PROJECT_ID, member, role);
    // Confirm that the member has been removed
    policy = Quickstart.getPolicy(crmService, PROJECT_ID);
    binding = null;
    bindings = policy.getBindings();
    for (Binding b : bindings) {
      if (b.getRole().equals(role)) {
        binding = b;
        break;
      }
    }
    if (binding != null) {
      assertThat(binding.getMembers(), not(hasItem(member)));
    }
  }
}
