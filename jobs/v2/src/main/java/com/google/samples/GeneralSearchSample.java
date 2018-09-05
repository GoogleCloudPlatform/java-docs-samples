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
import com.google.api.services.jobs.v2.model.CompensationEntry;
import com.google.api.services.jobs.v2.model.CompensationFilter;
import com.google.api.services.jobs.v2.model.CompensationInfo;
import com.google.api.services.jobs.v2.model.CompensationRange;
import com.google.api.services.jobs.v2.model.Job;
import com.google.api.services.jobs.v2.model.JobFilters;
import com.google.api.services.jobs.v2.model.JobQuery;
import com.google.api.services.jobs.v2.model.Money;
import com.google.api.services.jobs.v2.model.RequestMetadata;
import com.google.api.services.jobs.v2.model.SearchJobsRequest;
import com.google.api.services.jobs.v2.model.SearchJobsResponse;
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

  private static JobService jobService = JobServiceQuickstart.getJobService();

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

    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setMode("JOB_SEARCH"); // Set the search mode to a regular search

    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
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

    JobQuery jobQuery = new JobQuery().setCategories(categories);
    if (companyName != null) {
      jobQuery.setCompanyNames(Arrays.asList(companyName));
    }

    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setMode("JOB_SEARCH"); // Set the search mode to a regular search

    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
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

    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setMode("JOB_SEARCH"); // Set the search mode to a regular searchch

    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
  }
  // [END employment_types_filter]

  // [START date_range_filter]

  /**
   * Search on date range.
   */
  public static void dateRangeSearch(String companyName, String dateRange) throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            // Make sure to hash your userID
            .setUserId("HashedUserId")
            // Make sure to hash the sessionID
            .setSessionId("HashedSessionID")
            // Domain of the website where the search is conducted
            .setDomain("www.google.com");

    JobQuery jobQuery = new JobQuery().setPublishDateRange(dateRange);
    if (companyName != null) {
      jobQuery.setCompanyNames(Arrays.asList(companyName));
    }

    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setMode("JOB_SEARCH"); // Set the search mode to a regular search

    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
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

    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setMode("JOB_SEARCH"); // Set the search mode to a regular search

    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
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

    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setMode("JOB_SEARCH"); // Set the search mode to a regular search

    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
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
                            .setMax(new Money().setCurrencyCode("USD")
                                .setUnits(15L))
                            .setMin(
                                new Money()
                                    .setCurrencyCode("USD")
                                    .setUnits(10L)
                                    .setNanos(500000000))));
    if (companyName != null) {
      jobQuery.setCompanyNames(Arrays.asList(companyName));
    }

    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setMode("JOB_SEARCH"); // Set the search mode to a regular searchh

    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
  }
  // [END compensation_filter]

  public static void main(String... args) throws Exception {
    Company companyToBeCreated = BasicCompanySample.generateCompany().setDisplayName("Google");
    String companyName = BasicCompanySample.createCompany(companyToBeCreated).getName();

    Job jobToBeCreated = BasicJobSample.generateJobWithRequiredFields(companyName)
        .setJobTitle("Systems Administrator")
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
    dateRangeSearch(companyName, "PAST_24_HOURS");
    employmentTypesSearch(companyName, Arrays.asList("FULL_TIME", "CONTRACTOR", "PER_DIEM"));
    companyDisplayNameSearch(companyName, Arrays.asList("Google"));
    compensationSearch(companyName);
    languageCodeSearch(companyName, Arrays.asList("pt-BR", "en-US"));

    BasicJobSample.deleteJob(jobName);
    BasicCompanySample.deleteCompany(companyName);
  }
}

