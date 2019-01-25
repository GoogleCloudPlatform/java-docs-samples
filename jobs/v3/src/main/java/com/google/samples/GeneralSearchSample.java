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
import com.google.api.services.jobs.v3.model.CompensationEntry;
import com.google.api.services.jobs.v3.model.CompensationFilter;
import com.google.api.services.jobs.v3.model.CompensationInfo;
import com.google.api.services.jobs.v3.model.CompensationRange;
import com.google.api.services.jobs.v3.model.Job;
import com.google.api.services.jobs.v3.model.JobQuery;
import com.google.api.services.jobs.v3.model.Money;
import com.google.api.services.jobs.v3.model.RequestMetadata;
import com.google.api.services.jobs.v3.model.SearchJobsRequest;
import com.google.api.services.jobs.v3.model.SearchJobsResponse;
import com.google.api.services.jobs.v3.model.TimestampRange;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * The samples in this file introduce how to do a general search, including:
 *
 * - Basic keyword search
 *
 * - Filter on categories
 *
 * - Filter on employment types
 *
 * - Filter on date range
 *
 * - Filter on language codes
 *
 * - Filter on company display names
 *
 * - Filter on compensations
 */
public final class GeneralSearchSample {

  private static final String DEFAULT_PROJECT_ID =
      "projects/" + System.getenv("GOOGLE_CLOUD_PROJECT");

  private static CloudTalentSolution talentSolutionClient = JobServiceQuickstart
      .getTalentSolutionClient();

  //[START basic_keyword_search]

  /**
   * Simple search jobs with keyword.
   */
  public static void basicSearcJobs(String companyName, String query) throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            // Make sure to hash your userID
            .setUserId("HashedUserId")
            // Make sure to hash the sessionID
            .setSessionId("HashedSessionID")
            // Domain of the website where the search is conducted
            .setDomain("www.google.com");

    // Perform a search for analyst  related jobs
    JobQuery jobQuery = new JobQuery().setQuery(query);
    if (companyName != null) {
      jobQuery.setCompanyNames(Arrays.asList(companyName));
    }

    SearchJobsRequest searchJobsRequest =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setJobQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setSearchMode("JOB_SEARCH"); // Set the search mode to a regular search

    SearchJobsResponse searchJobsResponse =
        talentSolutionClient
            .projects()
            .jobs()
            .search(DEFAULT_PROJECT_ID, searchJobsRequest)
            .execute();

    System.out.println(searchJobsResponse);
  }
  //[END basic_keyword_search]

  // [START category_filter]

  /**
   * Search on category filter.
   */
  public static void categoryFilterSearch(String companyName, List<String> categories)
      throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            // Make sure to hash your userID
            .setUserId("HashedUserId")
            // Make sure to hash the sessionID
            .setSessionId("HashedSessionID")
            // Domain of the website where the search is conducted
            .setDomain("www.google.com");

    JobQuery jobQuery = new JobQuery().setJobCategories(categories);
    if (companyName != null) {
      jobQuery.setCompanyNames(Arrays.asList(companyName));
    }

    SearchJobsRequest searchJobsRequest =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setJobQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setSearchMode("JOB_SEARCH"); // Set the search mode to a regular search

    SearchJobsResponse searchJobsResponse =
        talentSolutionClient
            .projects()
            .jobs()
            .search(DEFAULT_PROJECT_ID, searchJobsRequest)
            .execute();

    System.out.println(searchJobsResponse);
  }
  // [END category_filter]

  // [START employment_types_filter]

  /**
   * Search on employment types.
   */
  public static void employmentTypesSearch(String companyName, List<String> employmentTypes)
      throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            // Make sure to hash your userID
            .setUserId("HashedUserId")
            // Make sure to hash the sessionID
            .setSessionId("HashedSessionID")
            // Domain of the website where the search is conducted
            .setDomain("www.google.com");

    JobQuery jobQuery = new JobQuery().setEmploymentTypes(employmentTypes);
    if (companyName != null) {
      jobQuery.setCompanyNames(Arrays.asList(companyName));
    }

    SearchJobsRequest searchJobsRequest =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setJobQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setSearchMode("JOB_SEARCH"); // Set the search mode to a regular searchch

    SearchJobsResponse searchJobsResponse =
        talentSolutionClient
            .projects()
            .jobs()
            .search(DEFAULT_PROJECT_ID, searchJobsRequest)
            .execute();

    System.out.println(searchJobsResponse);
  }
  // [END employment_types_filter]

  // [START date_range_filter]

  /**
   * Search on date range. In JSON format, the Timestamp type is encoded as a string in the [RFC
   * 3339](https://www.ietf.org/rfc/rfc3339.txt) format. That is, the format is
   * "{year}-{month}-{day}T{hour}:{min}:{sec}[.{frac_sec}]Z" e.g. "2017-01-15T01:30:15.01Z"
   */
  public static void dateRangeSearch(String companyName, String startTime, String endTime)
      throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            // Make sure to hash your userID
            .setUserId("HashedUserId")
            // Make sure to hash the sessionID
            .setSessionId("HashedSessionID")
            // Domain of the website where the search is conducted
            .setDomain("www.google.com");

    TimestampRange timestampRange =
        new TimestampRange().setStartTime(startTime).setEndTime(endTime);

    JobQuery jobQuery = new JobQuery().setPublishTimeRange(timestampRange);
    //JobQuery jobQuery = new JobQuery().setPublishTimeRange(dateRange);

    if (companyName != null) {
      jobQuery.setCompanyNames(Arrays.asList(companyName));
    }

    SearchJobsRequest searchJobsRequest =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setJobQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setSearchMode("JOB_SEARCH"); // Set the search mode to a regular search

    SearchJobsResponse searchJobsResponse =
        talentSolutionClient
            .projects()
            .jobs()
            .search(DEFAULT_PROJECT_ID, searchJobsRequest)
            .execute();

    System.out.println(searchJobsResponse);
  }
  // [END date_range_filter]

  // [START language_code_filter]

  /**
   * Search on language codes.
   */
  public static void languageCodeSearch(String companyName, List<String> languageCodes)
      throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            // Make sure to hash your userID
            .setUserId("HashedUserId")
            // Make sure to hash the sessionID
            .setSessionId("HashedSessionID")
            // Domain of the website where the search is conducted
            .setDomain("www.google.com");

    JobQuery jobQuery = new JobQuery().setLanguageCodes(languageCodes);
    if (companyName != null) {
      jobQuery.setCompanyNames(Arrays.asList(companyName));
    }

    SearchJobsRequest searchJobsRequest =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setJobQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setSearchMode("JOB_SEARCH"); // Set the search mode to a regular search

    SearchJobsResponse searchJobsResponse =
        talentSolutionClient
            .projects()
            .jobs()
            .search(DEFAULT_PROJECT_ID, searchJobsRequest)
            .execute();

    System.out.println(searchJobsResponse);
  }
  // [END language_code_filter]

  // [START company_display_name_filter]

  /**
   * Search on company display name.
   */
  public static void companyDisplayNameSearch(String companyName, List<String> companyDisplayNames)
      throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            // Make sure to hash your userID
            .setUserId("HashedUserId")
            // Make sure to hash the sessionID
            .setSessionId("HashedSessionID")
            // Domain of the website where the search is conducted
            .setDomain("www.google.com");

    JobQuery jobQuery = new JobQuery().setCompanyDisplayNames(companyDisplayNames);
    if (companyName != null) {
      jobQuery.setCompanyNames(Arrays.asList(companyName));
    }

    SearchJobsRequest searchJobsRequest =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setJobQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setSearchMode("JOB_SEARCH"); // Set the search mode to a regular search

    SearchJobsResponse searchJobsResponse =
        talentSolutionClient
            .projects()
            .jobs()
            .search(DEFAULT_PROJECT_ID, searchJobsRequest)
            .execute();

    System.out.println(searchJobsResponse);
  }
  // [END company_display_name_filter]

  // [START compensation_filter]

  /**
   * Search on compensation.
   */
  public static void compensationSearch(String companyName) throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            // Make sure to hash your userID
            .setUserId("HashedUserId")
            // Make sure to hash the sessionID
            .setSessionId("HashedSessionID")
            // Domain of the website where the search is conducted
            .setDomain("www.google.com");

    // Search jobs that pay between 10.50 and 15 USD per hour
    JobQuery jobQuery =
        new JobQuery()
            .setCompensationFilter(
                new CompensationFilter()
                    .setType("UNIT_AND_AMOUNT")
                    .setUnits(Arrays.asList("HOURLY"))
                    .setRange(
                        new CompensationRange()
                            .setMaxCompensation(new Money().setCurrencyCode("USD")
                                .setUnits(15L))
                            .setMinCompensation(
                                new Money()
                                    .setCurrencyCode("USD")
                                    .setUnits(10L)
                                    .setNanos(500000000))));
    if (companyName != null) {
      jobQuery.setCompanyNames(Arrays.asList(companyName));
    }

    SearchJobsRequest searchJobsRequest =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setJobQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setSearchMode("JOB_SEARCH"); // Set the search mode to a regular search

    SearchJobsResponse searchJobsResponse =
        talentSolutionClient
            .projects()
            .jobs()
            .search(DEFAULT_PROJECT_ID, searchJobsRequest)
            .execute();

    System.out.println(searchJobsResponse);
  }
  // [END compensation_filter]

  public static void main(String... args) throws Exception {
    Company companyToBeCreated = BasicCompanySample.generateCompany().setDisplayName("Google");
    String companyName = BasicCompanySample.createCompany(companyToBeCreated).getName();

    Job jobToBeCreated = BasicJobSample.generateJobWithRequiredFields(companyName)
        .setTitle("Systems Administrator")
        .setEmploymentTypes(Arrays.asList("FULL_TIME"))
        .setLanguageCode("en-US")
        .setCompensationInfo(
            new CompensationInfo().setEntries(Arrays.asList(
                new CompensationEntry()
                    .setType("BASE")
                    .setUnit("HOURLY")
                    .setAmount(new Money().setCurrencyCode("USD").setUnits(12L)))));
    final String jobName = BasicJobSample.createJob(jobToBeCreated).getName();

    // Wait several seconds for post processing
    Thread.sleep(10000);
    basicSearcJobs(companyName, "Systems Administrator");
    categoryFilterSearch(companyName, Arrays.asList("COMPUTER_AND_IT"));
    dateRangeSearch(companyName,
        "1980-01-15T01:30:15.01Z",
        "2099-01-15T01:30:15.01Z");
    employmentTypesSearch(companyName, Arrays.asList("FULL_TIME", "CONTRACTOR", "PER_DIEM"));
    companyDisplayNameSearch(companyName, Arrays.asList("Google"));
    compensationSearch(companyName);
    languageCodeSearch(companyName, Arrays.asList("pt-BR", "en-US"));

    BasicJobSample.deleteJob(jobName);
    BasicCompanySample.deleteCompany(companyName);
  }
}

