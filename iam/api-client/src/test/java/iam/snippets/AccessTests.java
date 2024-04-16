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

package iam.snippets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.cloud.iam.admin.v1.IAMClient;
import com.google.iam.admin.v1.DeleteServiceAccountRequest;
import com.google.iam.admin.v1.ServiceAccountName;
import com.google.iam.v1.Binding;
import com.google.iam.v1.Policy;
import org.junit.*;
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
    CreateServiceAccount.createServiceAccount(PROJECT_ID, SERVICE_ACCOUNT);
  }

  @AfterClass
  public static void removeUnusedStaff() throws IOException {
    try(IAMClient client = IAMClient.create()) {
      DeleteServiceAccountRequest request = DeleteServiceAccountRequest.newBuilder()
              .setName(ServiceAccountName.of(PROJECT_ID, SERVICE_ACCOUNT).toString() + "@" + PROJECT_ID + ".iam.gserviceaccount.com")
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
  public void testGetPolicy() throws IOException {
    GetPolicy.getPolicy(PROJECT_ID, SERVICE_ACCOUNT);
    String got = bout.toString();
    assertThat(got, containsString("Policy retrieved: "));
  }

  @Test
  public void testSetPolicy() throws IOException {
    Policy policy = GetPolicy.getPolicy(PROJECT_ID, SERVICE_ACCOUNT);
    SetPolicy.setPolicy(policy, PROJECT_ID, SERVICE_ACCOUNT);
    String got = bout.toString();
    assertThat(got, containsString("Policy retrieved: "));
  }

  @Test
  public void testAddBinding() {
    AddBinding.addBinding(policyMock);
    String got = bout.toString();
    assertThat(got, containsString("Added binding: "));
  }

  @Test
  public void testAddMember() {
    AddMember.addMember(policyMock);
    String got = bout.toString();
    assertThat(
            got,
            containsString("Member user:member-to-add@example.com added to role roles/existing-role"));
  }

  @Test
  public void testRemoveMember() {
    RemoveMember.removeMember(policyMock);
    String got = bout.toString();
    assertThat(
            got,
            containsString(
                    "Member user:member-to-remove@example.com removed from roles/existing-role"));
  }

  @Test
  public void testTestPermissions() {
    TestPermissions.testPermissions("projects/" + PROJECT_ID);
    String got = bout.toString();
    assertThat(
            got,
            containsString("Of the permissions listed in the request, the caller has the following: "));
  }
}
