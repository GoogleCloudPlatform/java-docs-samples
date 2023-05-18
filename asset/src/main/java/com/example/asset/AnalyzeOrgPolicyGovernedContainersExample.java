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

// [START asset_quickstart_analyze_org_policy_governed_containers]
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.asset.v1.AnalyzeOrgPolicyGovernedContainersRequest;
import com.google.cloud.asset.v1.AssetServiceClient;
import com.google.cloud.asset.v1.AssetServiceClient.AnalyzeOrgPolicyGovernedContainersPagedResponse;
import java.io.IOException;

public class AnalyzeOrgPolicyGovernedContainersExample {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace the ORG_ID with your Google Cloud Organization ID
    String scope = "organizations/ORG_ID";
    // TODO(developer): Replace the CONSTRAINT_NAME with the name of the constraint
    // you want to analyze. Find more Organization Policy Constraints at:
    // "http://cloud/resource-manager/docs/organization-policy/org-policy-constraints"
    String constraint = "constraints/CONSTRAINT_NAME";
    analyzeOrgPolicyGovernedContainers(scope, constraint);
  }

  // Analyzes containers governed by accessible Org policies that match a request.
  public static void analyzeOrgPolicyGovernedContainers(String scope, String constraint)
      throws Exception {
    AnalyzeOrgPolicyGovernedContainersRequest request =
        AnalyzeOrgPolicyGovernedContainersRequest.newBuilder()
            .setScope(scope)
            .setConstraint(constraint)
            .build();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (AssetServiceClient client = AssetServiceClient.create()) {
      AnalyzeOrgPolicyGovernedContainersPagedResponse response =
          client.analyzeOrgPolicyGovernedContainers(request);
      System.out.println(
          "AnalyzeOrgPolicyGovernedContainers completed successfully:\n"
              + response.getPage().getValues());
    }
  }
}
// [END asset_quickstart_analyze_org_policy_governed_containers]
