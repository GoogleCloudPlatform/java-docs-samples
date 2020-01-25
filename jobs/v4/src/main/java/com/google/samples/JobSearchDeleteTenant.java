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

import com.google.cloud.talent.v4beta1.DeleteTenantRequest;
import com.google.cloud.talent.v4beta1.TenantName;
import com.google.cloud.talent.v4beta1.TenantServiceClient;


public class JobSearchDeleteTenant {
  // [START job_search_delete_tenant]

  /** Delete Tenant */
  public static void deleteTenant(String projectId, String tenantId) {
    // [START job_search_delete_tenant_core]
    try (TenantServiceClient tenantServiceClient = TenantServiceClient.create()) {
      // projectId = "Your Google Cloud Project ID";
      // tenantId = "Your Tenant ID)";
      TenantName name = TenantName.of(projectId, tenantId);
      DeleteTenantRequest request =
          DeleteTenantRequest.newBuilder().setName(name.toString()).build();
      tenantServiceClient.deleteTenant(request);
      System.out.println("Deleted Tenant.");
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
    // [END job_search_delete_tenant_core]
  }
  // [END job_search_delete_tenant]

}
