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
import com.google.api.services.jobs.v3.model.Company;
import com.google.api.services.jobs.v3.model.Job;
import com.google.api.services.jobs.v3.model.JobQuery;
import com.google.api.services.jobs.v3.model.RequestMetadata;
import com.google.api.services.jobs.v3.model.SearchJobsRequest;
import com.google.api.services.jobs.v3.model.SearchJobsResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * The sample in this file introduce how to do a email alert search.
 */
public final class EmailAlertSearchSample {

  private static final String DEFAULT_PROJECT_ID =
      "projects/" + System.getenv("GOOGLE_CLOUD_PROJECT");

  private static CloudTalentSolution talentSolutionClient = JobServiceQuickstart
      .getTalentSolutionClient();
  // [START search_for_alerts]

  /**
   * Search jobs for alert.
   */
  public static void searchForAlerts(String companyName) throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            // Make sure to hash your userID
            .setUserId("HashedUserId")
            // Make sure to hash the sessionID
            .setSessionId("HashedSessionID")
            // Domain of the website where the search is conducted
            .setDomain("www.google.com");

    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setSearchMode("JOB_SEARCH"); // Set the search mode to a regular search
    if (companyName != null) {
      request.setJobQuery(new JobQuery().setCompanyNames(Arrays.asList(companyName)));
    }

    SearchJobsResponse response =
        talentSolutionClient
            .projects()
            .jobs()
            .searchForAlert(DEFAULT_PROJECT_ID, request)
            .execute();
    System.out.println(response);
  }
  // [END search_for_alerts]

  public static void main(String... args) throws Exception {
    Company companyToBeCreated = BasicCompanySample.generateCompany();
    String companyName = BasicCompanySample.createCompany(companyToBeCreated).getName();

    Job jobToBeCreated = BasicJobSample.generateJobWithRequiredFields(companyName);
    String jobName = BasicJobSample.createJob(jobToBeCreated).getName();

    // Wait several seconds for post processing
    Thread.sleep(10000);
    searchForAlerts(companyName);

    BasicJobSample.deleteJob(jobName);
    BasicCompanySample.deleteCompany(companyName);
  }
}
