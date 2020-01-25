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

import com.google.cloud.talent.v4beta1.CreateJobRequest;
import com.google.cloud.talent.v4beta1.Job;
import com.google.cloud.talent.v4beta1.JobServiceClient;
import com.google.cloud.talent.v4beta1.TenantName;
import com.google.cloud.talent.v4beta1.TenantOrProjectName;
import java.util.Arrays;
import java.util.List;


public class JobSearchCreateJob {
  // [START job_search_create_job]

  /**
   * Create Job
   *
   * @param projectId Your Google Cloud Project ID
   * @param tenantId Identifier of the Tenant
   */
  public static void createJob(
      String projectId,
      String tenantId,
      String companyName,
      String requisitionId,
      String title,
      String description,
      String jobApplicationUrl,
      String addressOne,
      String addressTwo,
      String languageCode) {
    // [START job_search_create_job_core]
    try (JobServiceClient jobServiceClient = JobServiceClient.create()) {
      // projectId = "Your Google Cloud Project ID";
      // tenantId = "Your Tenant ID (using tenancy is optional)";
      // companyName = "Company name, e.g. projects/your-project/companies/company-id";
      // requisitionId = "Job requisition ID, aka Posting ID. Unique per job.";
      // title = "Software Engineer";
      // description = "This is a description of this <i>wonderful</i> job!";
      // jobApplicationUrl = "https://www.example.org/job-posting/123";
      // addressOne = "1600 Amphitheatre Parkway, Mountain View, CA 94043";
      // addressTwo = "111 8th Avenue, New York, NY 10011";
      // languageCode = "en-US";
      TenantOrProjectName parent = TenantName.of(projectId, tenantId);
      List<String> uris = Arrays.asList(jobApplicationUrl);
      Job.ApplicationInfo applicationInfo =
          Job.ApplicationInfo.newBuilder().addAllUris(uris).build();
      List<String> addresses = Arrays.asList(addressOne, addressTwo);
      Job job =
          Job.newBuilder()
              .setCompany(companyName)
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
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
    // [END job_search_create_job_core]
  }
  // [END job_search_create_job]

}
