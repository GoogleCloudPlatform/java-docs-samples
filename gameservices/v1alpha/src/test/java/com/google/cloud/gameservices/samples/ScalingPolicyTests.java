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

import com.google.cloud.gameservices.samples.deployments.CreateDeployment;
import com.google.cloud.gameservices.samples.scalingpolicies.CreateScalingPolicy;
import com.google.cloud.gameservices.samples.scalingpolicies.DeleteScalingPolicy;
import com.google.cloud.gameservices.samples.scalingpolicies.GetScalingPolicy;
import com.google.cloud.gameservices.samples.scalingpolicies.ListScalingPolicies;
import com.google.cloud.gameservices.samples.scalingpolicies.UpdateScalingPolicy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ScalingPolicyTests {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");

  private static String parentName = String.format("projects/%s/locations/global", PROJECT_ID);

  private static String deploymentId = "deployment-1";
  private static String policyId = "policy-1";
  private static String policyName = String.format("%s/scalingPolicies/%s", parentName, policyId);

  private final PrintStream originalOut = System.out;
  private ByteArrayOutputStream bout;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @BeforeClass
  public static void init()
      throws InterruptedException, TimeoutException, IOException, ExecutionException {
    GameServicesTestUtil.deleteExistingScalingPolicies(parentName);
    GameServicesTestUtil.deleteExistingDeployments(parentName);
    CreateDeployment.createGameServerDeployment(PROJECT_ID, deploymentId);
    CreateScalingPolicy.createScalingPolicy(PROJECT_ID, policyId, deploymentId);
  }

  @After
  public void tearDown() {
    System.setOut(originalOut);
    bout.reset();
  }

  @AfterClass
  public static void tearDownClass() {
    GameServicesTestUtil.deleteExistingScalingPolicies(parentName);
    GameServicesTestUtil.deleteExistingDeployments(parentName);
  }

  @Test
  public void createDeleteScalingPolicyTest()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    String newPolicyId = "policy-2";
    String newPolicyName = String.format("%s/scalingPolicies/%s", parentName, newPolicyId);
    CreateScalingPolicy.createScalingPolicy(PROJECT_ID, newPolicyId, deploymentId);
    assertTrue(bout.toString().contains("Scaling Policy created: " + newPolicyName));

    DeleteScalingPolicy.deleteScalingPolicy(PROJECT_ID, newPolicyId);
    assertTrue(bout.toString().contains("Scaling Policy deleted: " + newPolicyName));
  }

  @Test
  public void getScalingPolicyTest() throws IOException {
    GetScalingPolicy.getScalingPolicy(PROJECT_ID, policyId);

    assertTrue(bout.toString().contains("Scaling Policy found: " + policyName));
  }

  @Test
  public void listScalingPoliciesTest() throws IOException {
    ListScalingPolicies.listScalingPolicies(PROJECT_ID);

    assertTrue(bout.toString().contains("Scaling Policy found: " + policyName));
  }

  @Test
  @Ignore("b/135051878")
  public void updateScalingPoliciesTest()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    UpdateScalingPolicy.updateScalingPolicy(PROJECT_ID, policyId);

    assertTrue(bout.toString().contains("Scaling Policy updated: " + policyName));
  }
}
