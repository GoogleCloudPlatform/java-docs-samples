/*
 * Copyright 2018 Google Inc.
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
import com.google.api.services.jobs.v2.model.CustomAttributeHistogramRequest;
import com.google.api.services.jobs.v2.model.HistogramFacets;
import com.google.api.services.jobs.v2.model.RequestMetadata;
import com.google.api.services.jobs.v2.model.SearchJobsRequest;
import com.google.api.services.jobs.v2.model.SearchJobsResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * Histogram search
 */
public final class HistogramSample {

  private static JobService jobService = JobServiceUtils.getJobService();

  // [START histogram_search]

  /**
   * Histogram search
   */
  public static void histogramSearch() throws IOException {
    HistogramFacets histogramFacets =
        new HistogramFacets()
            .setSimpleHistogramFacets(Arrays.asList("EMPLOYMENT_TYPE"))
            .setCustomAttributeHistogramFacets(
                Arrays.asList(
                    new CustomAttributeHistogramRequest()
                        .setKey("Visa_Type")
                        .setStringValueHistogram(true)));
    RequestMetadata requestMetadata =
        new RequestMetadata()
            .setUserId("HashedUserId") // Make sure to hash the userID
            .setSessionId("HashedSessionID") // Make sure to hash the sessionID
            .setDomain("www.google.com"); // This is the domain of the website where the search is
    // conducted.
    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setMode("JOB_SEARCH")
            .setHistogramFacets(histogramFacets);
    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
  }
  // [END histogram_search]

  public static void main(String... args) throws Exception {
    histogramSearch();
  }
}
