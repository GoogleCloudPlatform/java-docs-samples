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

import com.google.api.services.cloudresourcemanager.model.Binding;
import com.google.api.services.cloudresourcemanager.model.Policy;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
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

    policyMock = new Policy();
    List<String> members = new ArrayList<String>();
    members.add("user:member-to-remove@example.com");
    Binding binding = new Binding();
    binding.setRole("roles/existing-role");
    binding.setMembers(members);
    List<Binding> bindings = new ArrayList<Binding>();
    bindings.add(binding);
    policyMock.setBindings(bindings);
  }

  @After
  public void tearDown() {
    System.setOut(null);
    bout.reset();
  }

  @Test
  public void testGetPolicy() {
    GetPolicy.getPolicy(PROJECT_ID);
    String got = bout.toString();
    assertThat(got, containsString("Policy retrieved: "));
  }

  @Test
  public void testSetPolicy() {
    Policy policy = GetPolicy.getPolicy(PROJECT_ID);
    SetPolicy.setPolicy(policy, PROJECT_ID);
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
    TestPermissions.testPermissions(PROJECT_ID);
    String got = bout.toString();
    assertThat(
        got,
        containsString("Of the permissions listed in the request, the caller has the following: "));
  }
}
