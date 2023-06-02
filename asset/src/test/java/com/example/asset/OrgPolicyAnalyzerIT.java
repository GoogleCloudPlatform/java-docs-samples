/*
 * Copyright 2023 Google LLC
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

package com.example.asset;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

/* Tests for Org Policy Analyzer samples. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OrgPolicyAnalyzerIT {
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private PrintStream originalPrintStream;

  // Owner of the organization below: cloud-asset-analysis-team.
  private static String SCOPE = "organizations/474566717491";
  private static String CONSTRAINT_NAME = "constraints/compute.requireOsLogin";

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    originalPrintStream = System.out;
    System.setOut(out);
  }

  @After
  public void tearDown() {
    // restores print statements in the original method
    System.out.flush();
    System.setOut(originalPrintStream);
  }

  @Test
  public void testAnalyzeOrgPolicies() throws Exception {
    AnalyzeOrgPoliciesExample.analyzeOrgPolicies(SCOPE, CONSTRAINT_NAME);
    String got = bout.toString();
    assertThat(got).contains("consolidated_policy");
  }

  @Test
  public void testAnalyzeOrgPolicyGovernedAssets() throws Exception {
    AnalyzeOrgPolicyGovernedAssetsExample.analyzeOrgPolicyGovernedAssets(SCOPE, CONSTRAINT_NAME);
    String got = bout.toString();
    assertThat(got).contains("consolidated_policy");
  }

  @Test
  public void testAnalyzeOrgPolicyGovernedContainers() throws Exception {
    AnalyzeOrgPolicyGovernedContainersExample.analyzeOrgPolicyGovernedContainers(
        SCOPE, CONSTRAINT_NAME);
    String got = bout.toString();
    assertThat(got).contains("consolidated_policy");
  }
}
