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

package com.google.samples;

import com.google.cloud.talent.v4beta1.CreateTenantRequest;
import com.google.cloud.talent.v4beta1.ProjectName;
import com.google.cloud.talent.v4beta1.Tenant;
import com.google.cloud.talent.v4beta1.TenantServiceClient;


public class JobSearchCreateTenant {
  // [START job_search_create_tenant]

  public static void createTenant() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String externalId ="your-external-id";
    createTenant(projectId, externalId);
  }

  // Create Tenant for scoping resources, e.g. companies and jobs.
  public static void createTenant(String projectId, String externalId) {
    // [START job_search_create_tenant_core]
    try (TenantServiceClient tenantServiceClient = TenantServiceClient.create()) {
      // projectId = "Your Google Cloud Project ID";
      // externalId = "Your Unique Identifier for Tenant";
      ProjectName parent = ProjectName.of(projectId);
      Tenant tenant = Tenant.newBuilder().setExternalId(externalId).build();
      CreateTenantRequest request =
          CreateTenantRequest.newBuilder().setParent(parent.toString()).setTenant(tenant).build();
      Tenant response = tenantServiceClient.createTenant(request);
      System.out.println("Created Tenant");
      System.out.printf("Name: %s\n", response.getName());
      System.out.printf("External ID: %s\n", response.getExternalId());
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
    // [END job_search_create_tenant_core]
  }
  // [END job_search_create_tenant]

}
