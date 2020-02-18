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

import com.google.cloud.talent.v4beta1.CreateJobRequest;
import com.google.cloud.talent.v4beta1.Job;
import com.google.cloud.talent.v4beta1.JobServiceClient;
import com.google.cloud.talent.v4beta1.TenantName;
import com.google.cloud.talent.v4beta1.TenantOrProjectName;

public class JobSearchCreateJobCustomAttributes {
  // [START job_search_create_job_custom_attributes]

  /**
   * Create Job with Custom Attributes
   *
   * @param projectId Your Google Cloud Project ID
   * @param tenantId Identifier of the Tenantd
   */
  public static void createJob(
      String projectId,
      String tenantId,
      String companyName,
      String requisitionId,
      String languageCode) {
    // [START job_search_create_job_custom_attributes_core]
    try (JobServiceClient jobServiceClient = JobServiceClient.create()) {
      // projectId = "Your Google Cloud Project ID";
      // tenantId = "Your Tenant ID (using tenancy is optional)";
      // companyName = "Company name, e.g. projects/your-project/companies/company-id";
      // requisitionId = "Job requisition ID, aka Posting ID. Unique per job.";
      // languageCode = "en-US";
      TenantOrProjectName parent = TenantName.of(projectId, tenantId);
      Job job =
          Job.newBuilder()
              .setCompany(companyName)
              .setRequisitionId(requisitionId)
              .setLanguageCode(languageCode)
              .build();
      CreateJobRequest request =
          CreateJobRequest.newBuilder().setParent(parent.toString()).setJob(job).build();
      Job response = jobServiceClient.createJob(request);
      System.out.printf("Created job: %s\n", response.getName());
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
    // [END job_search_create_job_custom_attributes_core]
  }
  // [END job_search_create_job_custom_attributes]

}
