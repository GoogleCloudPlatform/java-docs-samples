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

import com.google.cloud.talent.v4beta1.DeleteJobRequest;
import com.google.cloud.talent.v4beta1.JobName;
import com.google.cloud.talent.v4beta1.JobServiceClient;
import com.google.cloud.talent.v4beta1.JobWithTenantName;


public class JobSearchDeleteJob {
  // [START job_search_delete_job]

  /** Delete Job */
  public static void deleteJob(String projectId, String tenantId, String jobId) {
    // [START job_search_delete_job_core]
    try (JobServiceClient jobServiceClient = JobServiceClient.create()) {
      // projectId = "Your Google Cloud Project ID";
      // tenantId = "Your Tenant ID (using tenancy is optional)";
      // jobId = "Company ID";
      JobName name = JobWithTenantName.of(projectId, tenantId, jobId);
      DeleteJobRequest request = DeleteJobRequest.newBuilder().setName(name.toString()).build();
      jobServiceClient.deleteJob(request);
      System.out.println("Deleted job.");
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
    // [END job_search_delete_job_core]
  }
  // [END job_search_delete_job]

}
