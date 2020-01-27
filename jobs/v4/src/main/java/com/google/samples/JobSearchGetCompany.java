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

import com.google.cloud.talent.v4beta1.Company;
import com.google.cloud.talent.v4beta1.CompanyName;
import com.google.cloud.talent.v4beta1.CompanyServiceClient;
import com.google.cloud.talent.v4beta1.CompanyWithTenantName;
import com.google.cloud.talent.v4beta1.GetCompanyRequest;


public class JobSearchGetCompany {
  // [START job_search_get_company]

  public static void getCompany() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String tenantId = "your-tenant-id";
    String companyId = "your-company-id";
    getCompany(projectId, tenantId, companyId);
  }

  // Get Company.
  public static void getCompany(String projectId, String tenantId, String companyId) {
    // [START job_search_get_company_core]
    try (CompanyServiceClient companyServiceClient = CompanyServiceClient.create()) {
      // projectId = "Your Google Cloud Project ID";
      // tenantId = "Your Tenant ID (using tenancy is optional)";
      // companyId = "Company ID";
      CompanyName name = CompanyWithTenantName.of(projectId, tenantId, companyId);
      GetCompanyRequest request = GetCompanyRequest.newBuilder().setName(name.toString()).build();
      Company response = companyServiceClient.getCompany(request);
      System.out.printf("Company name: %s\n", response.getName());
      System.out.printf("Display name: %s\n", response.getDisplayName());
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
    // [END job_search_get_company_core]
  }
  // [END job_search_get_company]

}
