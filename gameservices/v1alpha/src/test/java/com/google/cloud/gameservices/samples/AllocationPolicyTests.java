/*
 * Copyright 2019 Google LLC
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

package com.google.cloud.gameservices.samples;

import static org.junit.Assert.assertTrue;

import com.google.cloud.gameservices.samples.allocationpolicies.CreateAllocationPolicy;
import com.google.cloud.gameservices.samples.allocationpolicies.DeleteAllocationPolicy;
import com.google.cloud.gameservices.samples.allocationpolicies.GetAllocationPolicy;
import com.google.cloud.gameservices.samples.allocationpolicies.ListAllocationPolicies;
import com.google.cloud.gameservices.samples.allocationpolicies.UpdateAllocationPolicy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AllocationPolicyTests {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");

  private static String parentName = String.format("projects/%s/locations/global", PROJECT_ID);

  private static String policyId = "policy-1";
  private static String policyName = String.format(
      "%s/allocationPolicies/%s", parentName, policyId);

  private final PrintStream originalOut = System.out;
  private ByteArrayOutputStream bout;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @BeforeClass
  public static void init()
      throws InterruptedException, ExecutionException, TimeoutException, IOException {
    GameServicesTestUtil.deleteExistingAllocationPolicies(parentName);
    CreateAllocationPolicy.createAllocationPolicy(PROJECT_ID, policyId);
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
    bout.reset();
  }

  @AfterClass
  public static void tearDownClass() {
    GameServicesTestUtil.deleteExistingAllocationPolicies(parentName);
  }

  @Test
  public void createDeleteAllocationPolicyTest()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    String newPolicyId = "policy-2";
    String newPolicyName = String.format("%s/allocationPolicies/%s", parentName, newPolicyId);
    CreateAllocationPolicy.createAllocationPolicy(PROJECT_ID, newPolicyId);
    DeleteAllocationPolicy.deleteAllocationPolicy(PROJECT_ID, newPolicyId);
    String output = bout.toString();
    assertTrue(output.contains("Allocation Policy created: " + newPolicyName));
    assertTrue(output.contains("Allocation Policy deleted: " + newPolicyName));
  }

  @Test
  public void getAllocationPolicyTest() throws IOException {
    GetAllocationPolicy.getAllocationPolicy(PROJECT_ID, policyId);

    assertTrue(bout.toString().contains("Allocation Policy found: " + policyName));
  }

  @Test
  public void listAllocationPoliciesTest() throws IOException {
    ListAllocationPolicies.listAllocationPolicies(PROJECT_ID);

    assertTrue(bout.toString().contains("Allocation Policy found: " + policyName));
  }

  @Test
  public void updateAllocationPoliciesTest()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    UpdateAllocationPolicy.updateAllocationPolicy(PROJECT_ID, policyId);

    assertTrue(bout.toString().contains("Allocation Policy updated: " + policyName));
  }
}
