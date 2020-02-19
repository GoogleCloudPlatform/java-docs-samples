/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.examples.talent.v4beta1;

// [START job_search_list_tenants]

import com.google.cloud.talent.v4beta1.ListTenantsRequest;
import com.google.cloud.talent.v4beta1.ProjectName;
import com.google.cloud.talent.v4beta1.Tenant;
import com.google.cloud.talent.v4beta1.TenantServiceClient;

import java.io.IOException;

public class JobSearchListTenants {

  public static void listTenants() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    listTenants(projectId);
  }

  // List Tenants.
  public static void listTenants(String projectId) throws IOException {
    try (TenantServiceClient tenantServiceClient = TenantServiceClient.create()) {
      ProjectName parent = ProjectName.of(projectId);

      ListTenantsRequest request = ListTenantsRequest.newBuilder().setParent(parent.toString()).build();

      for (Tenant responseItem : tenantServiceClient.listTenants(request).iterateAll()) {
        System.out.printf("Tenant Name: %s\n", responseItem.getName());
        System.out.printf("External ID: %s\n", responseItem.getExternalId());
      }
    }
  }
}
// [END job_search_list_tenants]
