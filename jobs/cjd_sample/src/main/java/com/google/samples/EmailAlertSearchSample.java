package com.google.samples;

import com.google.api.services.jobs.v2.JobService;
import com.google.api.services.jobs.v2.model.RequestMetadata;
import com.google.api.services.jobs.v2.model.SearchJobsRequest;
import com.google.api.services.jobs.v2.model.SearchJobsResponse;
import java.io.IOException;

/**
 * Search for alerts.
 */
public final class EmailAlertSearchSample {

  private static JobService jobService = JobServiceUtils.getJobService();

  // [START search_for_alerts]

  /**
   * Search jobs for alert.
   */
  public static void searchForAlerts() throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            .setUserId("HashedUserId") // Make sure to hash the userID
            .setSessionId("HashedSessionID") // Make sure to hash the sessionID
            .setDomain("www.google.com"); // Domain of the website where the search is conducted

    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setMode("JOB_SEARCH"); // Set the search mode to a regular search

    SearchJobsResponse response = jobService.jobs().searchForAlert(request).execute();
    System.out.println(response);
  }
  // [END search_for_alerts]

  public static void main(String... args) throws Exception {
    searchForAlerts();
  }
}
