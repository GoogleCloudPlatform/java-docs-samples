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

package com.example.jobs;

import com.google.cloud.talent.v4beta1.Job;
import com.google.cloud.talent.v4beta1.JobServiceClient;
import com.google.cloud.talent.v4beta1.ListJobsRequest;
import com.google.cloud.talent.v4beta1.TenantName;
import com.google.cloud.talent.v4beta1.TenantOrProjectName;

public class JobSearchListJobs {
  // [START job_search_list_jobs]

  /**
   * List Jobs
   *
   * @param projectId Your Google Cloud Project ID
   * @param tenantId Identifier of the Tenant
   */
  public static void listJobs(String projectId, String tenantId, String filter) {
    // [START job_search_list_jobs_core]
    try (JobServiceClient jobServiceClient = JobServiceClient.create()) {
      // projectId = "Your Google Cloud Project ID";
      // tenantId = "Your Tenant ID (using tenancy is optional)";
      // filter = "companyName=projects/my-project/companies/company-id";
      TenantOrProjectName parent = TenantName.of(projectId, tenantId);
      ListJobsRequest request =
          ListJobsRequest.newBuilder().setParent(parent.toString()).setFilter(filter).build();
      for (Job responseItem : jobServiceClient.listJobs(request).iterateAll()) {
        System.out.printf("Job name: %s\n", responseItem.getName());
        System.out.printf("Job requisition ID: %s\n", responseItem.getRequisitionId());
        System.out.printf("Job title: %s\n", responseItem.getTitle());
        System.out.printf("Job description: %s\n", responseItem.getDescription());
      }
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
    // [END job_search_list_jobs_core]
  }
  // [END job_search_list_jobs]

}
