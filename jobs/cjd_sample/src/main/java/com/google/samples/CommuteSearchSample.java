package com.google.samples;

import com.google.api.services.jobs.v2.JobService;
import com.google.api.services.jobs.v2.model.CommutePreference;
import com.google.api.services.jobs.v2.model.JobQuery;
import com.google.api.services.jobs.v2.model.LatLng;
import com.google.api.services.jobs.v2.model.RequestMetadata;
import com.google.api.services.jobs.v2.model.SearchJobsRequest;
import com.google.api.services.jobs.v2.model.SearchJobsResponse;
import java.io.IOException;

/**
 * Commute Search
 */
public final class CommuteSearchSample {

  private static JobService jobService = JobServiceUtils.getJobService();

  // [START commute_search]

  /**
   * Commute search
   */
  public static void commuteSearch() throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            .setUserId("HashedUserId") // Make sure to hash the userID
            .setSessionId("HashedSessionID") // Make sure to hash the sessionID
            .setDomain("www.google.com"); // Domain of the website where the search is conducted
    JobQuery jobQuery =
        new JobQuery()
            .setCommuteFilter(
                new CommutePreference()
                    .setRoadTraffic("TRAFFIC_FREE")
                    .setMethod("TRANSIT")
                    .setTravelTime("1000s")
                    .setStartLocation(
                        new LatLng().setLatitude(37.422408).setLongitude(-122.085609)));
    SearchJobsRequest searchJobsRequest =
        new SearchJobsRequest()
            .setQuery(jobQuery)
            .setRequestMetadata(requestMetadata)
            .setJobView("FULL")
            .setEnablePreciseResultSize(true);
    SearchJobsResponse response = jobService.jobs().search(searchJobsRequest).execute();
    System.out.println(response);
  }
  // [END commute_search]

  public static void main(String... args) throws Exception {
    commuteSearch();
  }
}
