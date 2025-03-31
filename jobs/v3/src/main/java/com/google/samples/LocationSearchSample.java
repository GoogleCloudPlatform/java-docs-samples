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
import com.google.api.services.jobs.v3.model.LocationFilter;
import com.google.api.services.jobs.v3.model.RequestMetadata;
import com.google.api.services.jobs.v3.model.SearchJobsRequest;
import com.google.api.services.jobs.v3.model.SearchJobsResponse;
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

  private static final String DEFAULT_PROJECT_ID =
      "projects/" + System.getenv("GOOGLE_CLOUD_PROJECT");

  private static CloudTalentSolution talentSolutionClient =
      JobServiceQuickstart.getTalentSolutionClient();

  // [START job_basic_location_search]

  /** Basic location Search */
  public static void basicLocationSearch(String companyName, String location, double distance)
      throws IOException, InterruptedException {
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
        new LocationFilter().setAddress(location).setDistanceInMiles(distance);
    JobQuery jobQuery = new JobQuery().setLocationFilters(Arrays.asList(locationFilter));
    if (companyName != null) {
      jobQuery.setCompanyNames(Arrays.asList(companyName));
    }
    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setJobQuery(jobQuery)
            .setSearchMode("JOB_SEARCH");
    SearchJobsResponse response =
        talentSolutionClient.projects().jobs().search(DEFAULT_PROJECT_ID, request).execute();
    Thread.sleep(1000);
    System.out.printf("Basic location search results: %s", response);

  }
  // [END job_basic_location_search]

  // [START job_keyword_location_search]

  /** Keyword location Search */
  public static void keywordLocationSearch(
      String companyName, String location, double distance, String keyword)
      throws IOException, InterruptedException {
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
        new LocationFilter().setAddress(location).setDistanceInMiles(distance);
    JobQuery jobQuery =
        new JobQuery().setQuery(keyword).setLocationFilters(Arrays.asList(locationFilter));
    if (companyName != null) {
      jobQuery.setCompanyNames(Arrays.asList(companyName));
    }
    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setJobQuery(jobQuery)
            .setSearchMode("JOB_SEARCH");
    SearchJobsResponse response =
        talentSolutionClient.projects().jobs().search(DEFAULT_PROJECT_ID, request).execute();
    Thread.sleep(1000);
    System.out.printf("Keyword location search results: %s", response);
  }
  // [END job_keyword_location_search]

  // [START job_city_location_search]

  /** City location Search */
  public static void cityLocationSearch(String companyName, String location)
      throws IOException, InterruptedException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            // Make sure to hash the userID
            .setUserId("HashedUserId")
            // Make sure to hash the sessionID
            .setSessionId("HashedSessionID")
            // Domain of the website where the search is conducted
            .setDomain("www.google.com");
    LocationFilter locationFilter = new LocationFilter().setAddress(location);
    JobQuery jobQuery = new JobQuery().setLocationFilters(Arrays.asList(locationFilter));
    if (companyName != null) {
      jobQuery.setCompanyNames(Arrays.asList(companyName));
    }
    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setJobQuery(jobQuery)
            .setSearchMode("JOB_SEARCH");
    SearchJobsResponse response =
        talentSolutionClient.projects().jobs().search(DEFAULT_PROJECT_ID, request).execute();
    Thread.sleep(1000);
    System.out.printf("City locations search results: %s", response);
  }
  // [END job_city_location_search]

  // [START job_multi_locations_search]

  /** Multiple locations Search */
  public static void multiLocationsSearch(
      String companyName, String location1, double distance1, String location2)
      throws IOException, InterruptedException {
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
                    new LocationFilter().setAddress(location1).setDistanceInMiles(distance1),
                    new LocationFilter().setAddress(location2)));
    if (companyName != null) {
      jobQuery.setCompanyNames(Arrays.asList(companyName));
    }
    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setJobQuery(jobQuery)
            .setSearchMode("JOB_SEARCH");
    SearchJobsResponse response =
        talentSolutionClient.projects().jobs().search(DEFAULT_PROJECT_ID, request).execute();
    Thread.sleep(1000);
    System.out.printf("Multiple locations search results: %s", response);

  }
  // [END job_multi_locations_search]

  // [START job_broadening_location_search]

  /** Broadening location Search */
  public static void broadeningLocationsSearch(String companyName, String location)
      throws IOException, InterruptedException {
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
        new JobQuery().setLocationFilters(Arrays.asList(new LocationFilter().setAddress(location)));
    if (companyName != null) {
      jobQuery.setCompanyNames(Arrays.asList(companyName));
    }
    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setJobQuery(jobQuery)
            .setEnableBroadening(true)
            .setSearchMode("JOB_SEARCH");
    SearchJobsResponse response =
        talentSolutionClient.projects().jobs().search(DEFAULT_PROJECT_ID, request).execute();
    Thread.sleep(1000);
    System.out.printf("Broadening locations search results: %s", response);
  }
  // [END job_broadening_location_search]

  public static void main(String... args) throws Exception {
    String location = args.length >= 1 ? args[0] : "Mountain View, CA";
    double distance = args.length >= 2 ? Double.parseDouble(args[1]) : 0.5;
    String keyword = args.length >= 3 ? args[2] : "Software Engineer";
    String location2 = args.length >= 4 ? args[3] : "Sunnyvale, CA";

    Company companyToBeCreated = BasicCompanySample.generateCompany();
    String companyName = BasicCompanySample.createCompany(companyToBeCreated).getName();

    Job jobToBeCreated =
        BasicJobSample.generateJobWithRequiredFields(companyName)
            .setAddresses(Arrays.asList(location))
            .setTitle(keyword);
    final String jobName = BasicJobSample.createJob(jobToBeCreated).getName();
    Job jobToBeCreated2 =
        BasicJobSample.generateJobWithRequiredFields(companyName)
            .setAddresses(Arrays.asList(location2))
            .setTitle(keyword);
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
