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

import com.google.api.services.jobs.v2.JobService;
import com.google.api.services.jobs.v2.model.Company;
import com.google.api.services.jobs.v2.model.CustomAttributeHistogramRequest;
import com.google.api.services.jobs.v2.model.HistogramFacets;
import com.google.api.services.jobs.v2.model.Job;
import com.google.api.services.jobs.v2.model.JobQuery;
import com.google.api.services.jobs.v2.model.RequestMetadata;
import com.google.api.services.jobs.v2.model.SearchJobsRequest;
import com.google.api.services.jobs.v2.model.SearchJobsResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * The sample in this file introduce how to do a histogram search.
 */
public final class HistogramSample {

  private static JobService jobService = JobServiceQuickstart.getJobService();

  // [START histogram_search]

  /**
   * Histogram search
   */
  public static void histogramSearch(String companyName) throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            // Make sure to hash your userID
            .setUserId("HashedUserId")
            // Make sure to hash the sessionID
            .setSessionId("HashedSessionID")
            // Domain of the website where the search is conducted
            .setDomain(
                "www.google.com");

    HistogramFacets histogramFacets =
        new HistogramFacets()
            .setSimpleHistogramFacets(Arrays.asList("COMPANY_ID"))
            .setCustomAttributeHistogramFacets(
                Arrays.asList(
                    new CustomAttributeHistogramRequest()
                        .setKey("someFieldName1")
                        .setStringValueHistogram(true)));

    // conducted.
    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setMode("JOB_SEARCH")
            .setHistogramFacets(histogramFacets);
    if (companyName != null) {
      request.setQuery(new JobQuery().setCompanyNames(Arrays.asList(companyName)));
    }

    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
  }
  // [END histogram_search]

  public static void main(String... args) throws Exception {
    Company companyToBeCreated = BasicCompanySample.generateCompany();
    String companyName = BasicCompanySample.createCompany(companyToBeCreated).getName();

    Job jobToBeCreated = CustomAttributeSample.generateJobWithACustomAttribute(companyName);
    String jobName = BasicJobSample.createJob(jobToBeCreated).getName();

    // Wait several seconds for post processing
    Thread.sleep(10000);
    histogramSearch(companyName);

    BasicJobSample.deleteJob(jobName);
    BasicCompanySample.deleteCompany(companyName);
  }
}
