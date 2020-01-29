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

// [START job_search_create_job_beta]

import com.google.cloud.talent.v4beta1.CreateJobRequest;
import com.google.cloud.talent.v4beta1.Job;
import com.google.cloud.talent.v4beta1.JobServiceClient;
import com.google.cloud.talent.v4beta1.TenantName;
import com.google.cloud.talent.v4beta1.TenantOrProjectName;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class JobSearchCreateJob {

  public static void createJob() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String tenantId = "your-tenant-id";
    String companyId = "your-company-id";
    String requisitionId = "your-unique-req-id";
    String title = "your-job-title";
    String description = "your-job-description";
    String jobApplicationUrl = "your-job-url";
    String addressOne = "your-job-address-1";
    String addressTwo = "your-job-address-2";
    String languageCode = "your-lang-code";

    createJob(
        projectId,
        tenantId,
        companyId,
        requisitionId,
        title,
        description,
        jobApplicationUrl,
        addressOne,
        addressTwo,
        languageCode);
  }

  // Create a job.
  public static void createJob(
      String projectId,
      String tenantId,
      String companyId,
      String requisitionId,
      String title,
      String description,
      String jobApplicationUrl,
      String addressOne,
      String addressTwo,
      String languageCode)
      throws IOException {
    try (JobServiceClient jobServiceClient = JobServiceClient.create()) {
      TenantOrProjectName parent = TenantName.of(projectId, tenantId);
      List<String> uris = Arrays.asList(jobApplicationUrl);
      Job.ApplicationInfo applicationInfo =
          Job.ApplicationInfo.newBuilder().addAllUris(uris).build();
      List<String> addresses = Arrays.asList(addressOne, addressTwo);
      Job job =
          Job.newBuilder()
              .setCompany(companyId)
              .setRequisitionId(requisitionId)
              .setTitle(title)
              .setDescription(description)
              .setApplicationInfo(applicationInfo)
              .addAllAddresses(addresses)
              .setLanguageCode(languageCode)
              .build();

      CreateJobRequest request =
          CreateJobRequest.newBuilder().setParent(parent.toString()).setJob(job).build();

      Job response = jobServiceClient.createJob(request);
      System.out.printf("Created job: %s\n", response.getName());
    }
  }
}
// [END job_search_create_job_beta]
