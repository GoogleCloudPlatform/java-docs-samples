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
import com.google.api.services.jobs.v2.model.Job;
import com.google.api.services.jobs.v2.model.JobQuery;
import com.google.api.services.jobs.v2.model.LocationFilter;
import com.google.api.services.jobs.v2.model.RequestMetadata;
import com.google.api.services.jobs.v2.model.SearchJobsRequest;
import com.google.api.services.jobs.v2.model.SearchJobsResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * The samples in this file introduce how to do a search with location filter, including:
 *
 * - Basic search with location filter
 *
 * - Keyword search with location filter
 *
 * - Location filter on city level
 *
 * - Broadening search with location filter
 *
 * - Location filter of multiple locations
 */
public final class LocationSearchSample {

  private static JobService jobService = JobServiceQuickstart.getJobService();

  // [START basic_location_search]

  /**
   * Basic location Search
   */
  public static void basicLocationSearch(String companyName, String location, double distance)
      throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            // Make sure to hash the userID
            .setUserId("HashedUserId")
            // Make sure to hash the sessionID
            .setSessionId("HashedSessionID")
            // Domain of the website where the search is conducted
            .setDomain("www.google.com");
    LocationFilter locationFilter =
        new LocationFilter().setName(location)
            .setDistanceInMiles(distance);
    JobQuery jobQuery = new JobQuery()
        .setLocationFilters(Arrays.asList(locationFilter));
    if (companyName != null) {
      jobQuery.setCompanyNames(Arrays.asList(companyName));
    }
    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery)
            .setMode("JOB_SEARCH");
    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
  }
  // [END basic_location_search]

  // [START keyword_location_search]

  /**
   * Keyword location Search
   */
  public static void keywordLocationSearch(String companyName, String location, double distance,
      String keyword)
      throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            // Make sure to hash the userID
            .setUserId("HashedUserId")
            // Make sure to hash the sessionID
            .setSessionId("HashedSessionID")
            // Domain of the website where the search is conducted
            .setDomain("www.google.com");
    LocationFilter locationFilter =
        new LocationFilter().setName(location)
            .setDistanceInMiles(distance);
    JobQuery jobQuery =
        new JobQuery()
            .setQuery(keyword)
            .setLocationFilters(Arrays.asList(locationFilter));
    if (companyName != null) {
      jobQuery.setCompanyNames(Arrays.asList(companyName));
    }
    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery)
            .setMode("JOB_SEARCH");
    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
  }
  // [END keyword_location_search]

  // [START city_location_search]

  /**
   * City location Search
   */
  public static void cityLocationSearch(String companyName, String location) throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            // Make sure to hash the userID
            .setUserId("HashedUserId")
            // Make sure to hash the sessionID
            .setSessionId("HashedSessionID")
            // Domain of the website where the search is conducted
            .setDomain("www.google.com");
    LocationFilter locationFilter = new LocationFilter()
        .setName(location);
    JobQuery jobQuery = new JobQuery()
        .setLocationFilters(Arrays.asList(locationFilter));
    if (companyName != null) {
      jobQuery.setCompanyNames(Arrays.asList(companyName));
    }
    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery)
            .setMode("JOB_SEARCH");
    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
  }
  // [END city_location_search]

  // [START multi_locations_search]

  /**
   * Multiple locations Search
   */
  public static void multiLocationsSearch(String companyName, String location1, double distance1,
      String location2)
      throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            // Make sure to hash the userID
            .setUserId("HashedUserId")
            // Make sure to hash the sessionID
            .setSessionId("HashedSessionID")
            // Domain of the website where the search is conducted
            .setDomain("www.google.com");
    JobQuery jobQuery =
        new JobQuery()
            .setLocationFilters(
                Arrays.asList(
                    new LocationFilter().setName(location1).setDistanceInMiles(distance1),
                    new LocationFilter().setName(location2)));
    if (companyName != null) {
      jobQuery.setCompanyNames(Arrays.asList(companyName));
    }
    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery)
            .setMode("JOB_SEARCH");
    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
  }
  // [END multi_locations_search]

  // [START broadening_location_search]

  /**
   * Broadening location Search
   */
  public static void broadeningLocationsSearch(String companyName, String location)
      throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            // Make sure to hash the userID
            .setUserId("HashedUserId")
            // Make sure to hash the sessionID
            .setSessionId("HashedSessionID")
            // Domain of the website where the search is conducted
            .setDomain("www.google.com");
    JobQuery jobQuery =
        new JobQuery()
            .setLocationFilters(Arrays
                .asList(new LocationFilter().setName(location)));
    if (companyName != null) {
      jobQuery.setCompanyNames(Arrays.asList(companyName));
    }
    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery)
            .setEnableBroadening(true)
            .setMode("JOB_SEARCH");
    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
  }
  // [END broadening_location_search]

  public static void main(String... args) throws Exception {
    String location = args.length >= 1 ? args[0] : "Mountain View, CA";
    double distance = args.length >= 2 ? Double.parseDouble(args[1]) : 0.5;
    String keyword = args.length >= 3 ? args[2] : "Software Engineer";
    String location2 = args.length >= 4 ? args[3] : "Sunnyvale, CA";

    Company companyToBeCreated = BasicCompanySample.generateCompany();
    String companyName = BasicCompanySample.createCompany(companyToBeCreated).getName();

    Job jobToBeCreated = BasicJobSample.generateJobWithRequiredFields(companyName)
        .setLocations(Arrays.asList(location)).setJobTitle(keyword);
    final String jobName = BasicJobSample.createJob(jobToBeCreated).getName();
    Job jobToBeCreated2 = BasicJobSample.generateJobWithRequiredFields(companyName)
        .setLocations(Arrays.asList(location2)).setJobTitle(keyword);
    final String jobName2 = BasicJobSample.createJob(jobToBeCreated2).getName();

    // Wait several seconds for post processing
    Thread.sleep(10000);
    basicLocationSearch(companyName, location, distance);
    cityLocationSearch(companyName, location);
    broadeningLocationsSearch(companyName, location);
    keywordLocationSearch(companyName, location, distance, keyword);
    multiLocationsSearch(companyName, location, distance, location2);

    BasicJobSample.deleteJob(jobName);
    BasicJobSample.deleteJob(jobName2);
    BasicCompanySample.deleteCompany(companyName);
  }
}
