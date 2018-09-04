/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples;

import com.google.api.services.jobs.v3.CloudTalentSolution;
import com.google.api.services.jobs.v3.model.ApplicationInfo;
import com.google.api.services.jobs.v3.model.Company;
import com.google.api.services.jobs.v3.model.Job;
import com.google.api.services.jobs.v3.model.JobQuery;
import com.google.api.services.jobs.v3.model.RequestMetadata;
import com.google.api.services.jobs.v3.model.SearchJobsRequest;
import com.google.api.services.jobs.v3.model.SearchJobsResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

/**
 * The sample in this file introduce featured job, including:
 *
 * - Construct a featured job
 *
 * - Search featured job
 */
public final class FeaturedJobsSearchSample {

  private static final String DEFAULT_PROJECT_ID =
      "projects/" + System.getenv("GOOGLE_CLOUD_PROJECT");

  private static CloudTalentSolution talentSolutionClient = JobServiceQuickstart
      .getTalentSolutionClient();

  // [START featured_job]

  /**
   * Creates a job as featured.
   */
  public static Job generateFeaturedJob(String companyName) throws IOException {
    // requisition id should be a unique Id in your system.
    String requisitionId =
        "featuredJob:" + String.valueOf(new Random().nextLong());
    ApplicationInfo applicationInfo =
        new ApplicationInfo().setUris(Arrays.asList("http://careers.google.com"));

    Job job =
        new Job()
            .setRequisitionId(requisitionId)
            .setTitle("Software Engineer")
            .setCompanyName(companyName)
            .setApplicationInfo(applicationInfo)
            .setDescription(
                "Design, develop, test, deploy, maintain and improve software.")
            // Featured job is the job with positive promotion value
            .setPromotionValue(2);
    System.out.println("Job generated: " + job);
    return job;
  }
  // [END featured_job]

  // [START search_featured_job]

  /**
   * Searches featured jobs.
   */
  public static void searchFeaturedJobs(String companyName) throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            // Make sure to hash your userID
            .setUserId("HashedUserId")
            // Make sure to hash the sessionID
            .setSessionId("HashedSessionID")
            // Domain of the website where the search is conducted
            .setDomain("www.google.com");

    JobQuery jobQuery = new JobQuery().setQuery("Software Engineer");
    if (companyName != null) {
      jobQuery.setCompanyNames(Arrays.asList(companyName));
    }

    SearchJobsRequest searchJobsRequest =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setJobQuery(jobQuery)
            // Set the search mode to a featured search,
            // which would only search the jobs with positive promotion value.
            .setSearchMode("FEATURED_JOB_SEARCH");
    SearchJobsResponse response =
        talentSolutionClient
            .projects()
            .jobs()
            .search(DEFAULT_PROJECT_ID, searchJobsRequest)
            .execute();
    System.out.println(response);
  }
  // [END search_featured_job]

  public static void main(String... args) throws Exception {
    Company companyToBeCreated = BasicCompanySample.generateCompany();
    String companyName = BasicCompanySample.createCompany(companyToBeCreated).getName();

    Job jobToBeCreated = generateFeaturedJob(companyName);
    String jobName = BasicJobSample.createJob(jobToBeCreated).getName();

    // Wait several seconds for post processing
    Thread.sleep(10000);
    searchFeaturedJobs(companyName);

    BasicJobSample.deleteJob(jobName);
    BasicCompanySample.deleteCompany(companyName);
  }
}
