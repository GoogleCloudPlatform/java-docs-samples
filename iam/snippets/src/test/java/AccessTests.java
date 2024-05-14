/* Copyright 2019 Google LLC
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.CreateServiceAccountRequest;
import com.google.iam.admin.v1.DeleteServiceAccountRequest;
import com.google.iam.admin.v1.ProjectName;
import com.google.iam.admin.v1.ServiceAccountName;
import com.google.iam.v1.Binding;
import com.google.iam.v1.Policy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AccessTests {

  private ByteArrayOutputStream bout;
  private Policy policyMock;
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String SERVICE_ACCOUNT =
          "service-account-" + UUID.randomUUID().toString().substring(0, 8);

  private static void requireEnvVar(String varName) {
    assertNotNull(
          System.getenv(varName),
          String.format("Environment variable '%s' is required to perform these tests.", varName));
  }

  @BeforeClass
  public static void checkRequirementsAndInitServiceAccount() throws IOException {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    CreateServiceAccountRequest request = CreateServiceAccountRequest.newBuilder()
            .setName(ProjectName.of(PROJECT_ID).toString())
            .setAccountId(SERVICE_ACCOUNT)
            .build();
    try (IAMClient iamClient = IAMClient.create()) {
      iamClient.createServiceAccount(request);
    }
  }

  @AfterClass
  public static void cleanup() throws IOException {
    try (IAMClient client = IAMClient.create()) {
      String serviceAccName = ServiceAccountName.of(PROJECT_ID, SERVICE_ACCOUNT).toString();
      DeleteServiceAccountRequest request = DeleteServiceAccountRequest.newBuilder()
              .setName(serviceAccName + "@" + PROJECT_ID + ".iam.gserviceaccount.com")
              .build();
      client.deleteServiceAccount(request);
    }
  }

  @Before
  public void beforeTest() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));

    List<String> members = new ArrayList<>();
    members.add("user:member-to-remove@example.com");
    Binding binding = Binding.newBuilder()
            .setRole("roles/existing-role")
            .addAllMembers(members)
            .build();
    List<Binding> bindings = new ArrayList<>();
    bindings.add(binding);

    policyMock = Policy.newBuilder()
            .addAllBindings(bindings)
            .build();
  }

  @After
  public void tearDown() {
    System.setOut(null);
    bout.reset();
  }

  @Test
  public void testGetServiceAccountPolicy() throws IOException {
    Policy policy = GetServiceAccountPolicy.getPolicy(PROJECT_ID, SERVICE_ACCOUNT);
    assertNotNull(policy);
    assertNotNull(policy.getEtag());
  }

  @Test
  public void testSetServiceAccountPolicy() throws IOException {
    Policy policy = GetServiceAccountPolicy.getPolicy(PROJECT_ID, SERVICE_ACCOUNT);
    Policy setPolicy = SetServiceAccountPolicy
            .setServiceAccountPolicy(policy, PROJECT_ID, SERVICE_ACCOUNT);
    assertThat("version of updated policy should be incremented",
            setPolicy.getVersion() > policy.getVersion()
    );
  }

  @Test
  public void testGetProjectPolicy() throws IOException {
    Policy policy = GetServiceAccountPolicy.getPolicy(PROJECT_ID, SERVICE_ACCOUNT);
    assertNotNull(policy);
    assertNotNull(policy.getEtag());
  }

  @Test
  public void testSetProjectPolicy() throws IOException {
    Policy policy = GetProjectPolicy.getProjectPolicy(PROJECT_ID);
    Policy setPolicy = SetProjectPolicy.setProjectPolicy(policy, PROJECT_ID);
    assertNotNull(setPolicy);
    assertNotNull(setPolicy.getEtag());
  }

  @Test
  public void testAddBinding() {
    String role = "roles/role-to-add";
    List<String> members = new ArrayList<>();
    members.add("user:member-to-add@example.com");
    policyMock = AddBinding.addBinding(policyMock, role, members);
    assertNotNull(policyMock);
    boolean bindingAdded = false;
    for (Binding b : policyMock.getBindingsList()) {
      if (b.getRole().equals(role) && b.getMembersList().containsAll(members)) {
        bindingAdded = true;
        break;
      }
    }
    assertThat("policy should contain new binding", bindingAdded);
  }

  @Test
  public void testAddMember() {
    String role = "roles/existing-role";
    String member = "user:member-to-add@example.com";
    policyMock = AddMember.addMember(policyMock, role, member);
    assertNotNull(policyMock);
    boolean memberAdded = false;
    for (Binding b : policyMock.getBindingsList()) {
      if (b.getRole().equals(role) && b.getMembersList().contains(member)) {
        memberAdded = true;
        break;
      }
    }
    assertThat("policy should contain role and new member", memberAdded);
  }

  @Test
  public void testRemoveMember() {
    String role = "roles/existing-role";
    String member = "user:member-to-add@example.com";
    policyMock = RemoveMember.removeMember(policyMock, role, member);
    assertNotNull(policyMock);
    boolean memberRemoved = true;
    for (Binding b : policyMock.getBindingsList()) {
      if (b.getRole().equals(role) && b.getMembersList().contains(member)) {
        memberRemoved = false;
        break;
      }
    }
    assertThat("policy should not contain member", memberRemoved);
  }
}
