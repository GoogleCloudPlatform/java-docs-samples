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

// [START job_search_create_job_custom_attributes]

import com.google.cloud.talent.v4beta1.CreateJobRequest;
import com.google.cloud.talent.v4beta1.Job;
import com.google.cloud.talent.v4beta1.JobServiceClient;
import com.google.cloud.talent.v4beta1.TenantName;
import com.google.cloud.talent.v4beta1.TenantOrProjectName;

import java.io.IOException;

public class JobSearchCreateJobCustomAttributes {

  public static void createJob() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String tenantId = "your-tenant-id";
    String companyId = "your-company-id";
    String requisitionId = "your-unique-req-id";
    createJob(projectId, tenantId, companyId, requisitionId);
  }

  // Create Job with Custom Attributes.
  public static void createJob(
      String projectId,
      String tenantId,
      String companyId,
      String requisitionId)
      throws IOException {
    try (JobServiceClient jobServiceClient = JobServiceClient.create()) {
      TenantOrProjectName parent = TenantName.of(projectId, tenantId);
      Job job =
              Job.newBuilder()
                      .setCompany(companyId)
                      .setTitle("Software Developer II")
                      .setDescription("This is a description of this <i>wonderful</i> job!")
                      .setRequisitionId(requisitionId)
                      .setLanguageCode("en-US")
                      .build();
      CreateJobRequest request =
              CreateJobRequest.newBuilder().setParent(parent.toString()).setJob(job).build();
      Job response = jobServiceClient.createJob(request);
      System.out.printf("Created job: %s\n", response.getName());
    }
  }
}
// [END job_search_create_job_custom_attributes]
