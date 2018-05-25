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
