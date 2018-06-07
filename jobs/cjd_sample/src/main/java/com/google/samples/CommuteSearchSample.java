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
