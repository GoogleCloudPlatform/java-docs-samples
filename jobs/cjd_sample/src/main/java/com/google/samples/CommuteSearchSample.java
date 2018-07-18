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
import com.google.api.services.jobs.v2.model.CommutePreference;
import com.google.api.services.jobs.v2.model.Company;
import com.google.api.services.jobs.v2.model.Job;
import com.google.api.services.jobs.v2.model.JobQuery;
import com.google.api.services.jobs.v2.model.LatLng;
import com.google.api.services.jobs.v2.model.RequestMetadata;
import com.google.api.services.jobs.v2.model.SearchJobsRequest;
import com.google.api.services.jobs.v2.model.SearchJobsResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * The samples in this file introduce how to do a commute search.
 *
 * Note: Commute Search is different from location search. It only take latitude and longitude as
 * the start location.
 */
public final class CommuteSearchSample {

  private static JobService jobService = JobServiceQuickstart.getJobService();

  // [START commute_search]

  public static void commuteSearch(String companyName) throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            // Make sure to hash your userID
            .setUserId("HashedUserId")
            // Make sure to hash the sessionID
            .setSessionId("HashedSessionID")
            // Domain of the website where the search is conducted
            .setDomain("www.google.com");
    JobQuery jobQuery =
        new JobQuery()
            .setCommuteFilter(
                new CommutePreference()
                    .setRoadTraffic("TRAFFIC_FREE")
                    .setMethod("TRANSIT")
                    .setTravelTime("1000s")
                    .setStartLocation(
                        new LatLng().setLatitude(37.422408)
                            .setLongitude(-122.085609)));
    if (companyName != null) {
      jobQuery.setCompanyNames(Arrays.asList(companyName));
    }
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
    Company companyToBeCreated = BasicCompanySample.generateCompany();
    String companyName = BasicCompanySample.createCompany(companyToBeCreated).getName();

    Job jobToBeCreated = BasicJobSample.generateJobWithRequiredFields(companyName)
        .setLocations(Arrays.asList("1600 Amphitheatre Pkwy, Mountain View, CA 94043"));
    String jobName = BasicJobSample.createJob(jobToBeCreated).getName();

    // Wait several seconds for post processing
    Thread.sleep(10000);
    commuteSearch(companyName);

    BasicJobSample.deleteJob(jobName);
    BasicCompanySample.deleteCompany(companyName);
  }
}
