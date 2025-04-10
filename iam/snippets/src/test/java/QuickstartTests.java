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

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertNotNull;

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.ServiceAccount;
import com.google.iam.v1.Binding;
import com.google.iam.v1.Policy;
import java.io.IOException;
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
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String SERVICE_ACCOUNT =
      "iam-test-account-" + UUID.randomUUID().toString().split("-")[0];
  private String serviceAccountEmail;

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
  public void setUp() throws IOException, InterruptedException {
    ServiceAccount serviceAccount =
        Util.setUpTest_createServiceAccount(PROJECT_ID, SERVICE_ACCOUNT);
    serviceAccountEmail = serviceAccount.getEmail();
  }

  // Deletes the service account used in the test.
  @After
  public void tearDown() throws IOException {
    Util.tearDownTest_deleteServiceAccount(PROJECT_ID, SERVICE_ACCOUNT);
  }

  @Test
  public void testQuickstart() throws Exception {
    String member = "serviceAccount:" + serviceAccountEmail;
    String role = "roles/viewer";
    String serviceAccountName = SERVICE_ACCOUNT + "@" + PROJECT_ID + ".iam.gserviceaccount.com";

    try (IAMClient iamClient = IAMClient.create()) {
      // Tests addBinding()
      Quickstart.addBinding(iamClient, PROJECT_ID, serviceAccountName, member, role);

      // Get the project's policy and confirm that the member is present in the policy
      Policy policy = Quickstart.getPolicy(iamClient, PROJECT_ID, serviceAccountName);
      Binding binding = null;
      List<Binding> bindings = policy.getBindingsList();
      for (Binding b : bindings) {
        if (b.getRole().equals(role)) {
          binding = b;
          break;
        }
      }
      assertNotNull(binding);
      assertThat(binding.getMembersList(), hasItem(member));

      // Tests removeMember()
      Quickstart.removeMember(iamClient, PROJECT_ID, serviceAccountName, member, role);
      // Confirm that the member has been removed
      policy = Quickstart.getPolicy(iamClient, PROJECT_ID, serviceAccountName);
      binding = null;
      bindings = policy.getBindingsList();
      for (Binding b : bindings) {
        if (b.getRole().equals(role)) {
          binding = b;
          break;
        }
      }
      if (binding != null) {
        assertThat(binding.getMembersList(), not(hasItem(member)));
      }
    }
  }
}
