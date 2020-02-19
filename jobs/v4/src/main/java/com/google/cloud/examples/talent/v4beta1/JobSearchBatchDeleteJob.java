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

import com.google.cloud.talent.v4beta1.BatchDeleteJobsRequest;
import com.google.cloud.talent.v4beta1.JobServiceClient;
import com.google.cloud.talent.v4beta1.TenantName;
import com.google.cloud.talent.v4beta1.TenantOrProjectName;

public class JobSearchBatchDeleteJob {

  // [START job_search_batch_delete_job]

  /**
   * Batch delete jobs using a filter
   *
   * @param projectId Your Google Cloud Project ID
   * @param tenantId Identifier of the Tenantd
   * @param filter The filter string specifies the jobs to be deleted. For example: companyName =
   *     "projects/api-test-project/companies/123" AND equisitionId = "req-1"
   */
  public static void batchDeleteJobs(String projectId, String tenantId, String filter) {
    // [START job_search_batch_delete_job_core]
    try (JobServiceClient jobServiceClient = JobServiceClient.create()) {
      // projectId = "Your Google Cloud Project ID";
      // tenantId = "Your Tenant ID (using tenancy is optional)";
      // filter = "[Query]";
      TenantOrProjectName parent = TenantName.of(projectId, tenantId);
      BatchDeleteJobsRequest request =
          BatchDeleteJobsRequest.newBuilder()
              .setParent(parent.toString())
              .setFilter(filter)
              .build();
      jobServiceClient.batchDeleteJobs(request);
      System.out.println("Batch deleted jobs from filter");
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
    // [END job_search_batch_delete_job_core]
  }
  // [END job_search_batch_delete_job]

}
