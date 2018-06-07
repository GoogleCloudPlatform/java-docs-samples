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
import com.google.api.services.jobs.v2.model.Company;
import com.google.api.services.jobs.v2.model.CompensationFilter;
import com.google.api.services.jobs.v2.model.CompensationRange;
import com.google.api.services.jobs.v2.model.Job;
import com.google.api.services.jobs.v2.model.JobQuery;
import com.google.api.services.jobs.v2.model.Money;
import com.google.api.services.jobs.v2.model.RequestMetadata;
import com.google.api.services.jobs.v2.model.SearchJobsRequest;
import com.google.api.services.jobs.v2.model.SearchJobsResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Search Filters.
 */
public final class SearchFiltersSample {

  private static JobService jobService = JobServiceUtils.getJobService();

  // [START category_filter]

  /**
   * Search on category filter.
   */
  public static void categoryFilterSearch() throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            .setUserId("HashedUserId") // Make sure to hash your userID
            .setSessionId("HashedSessionID") // Make sure to hash the sessionID
            .setDomain("www.google.com"); // Domain of the website where the search is conducted

    JobQuery jobQuery =
        new JobQuery()
            .setCategories(
                Arrays.asList(
                    "FARMING_AND_OUTDOORS", "ANIMAL_CARE", "SPORTS_FITNESS_AND_RECREATION"));

    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setMode("JOB_SEARCH"); // Set the search mode to a regular search

    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
  }
  // [END category_filter]

  // [START employment_types]

  /**
   * Search on employment types.
   */
  public static void employmentTypesSearch() throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            .setUserId("HashedUserId") // Make sure to hash your userID
            .setSessionId("HashedSessionID") // Make sure to hash the sessionID
            .setDomain("www.google.com"); // Domain of the website where the search is conducted

    JobQuery jobQuery =
        new JobQuery().setEmploymentTypes(Arrays.asList("FULL_TIME", "CONTRACTOR", "PER_DIEM"));

    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setMode("JOB_SEARCH"); // Set the search mode to a regular search

    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
  }
  // [END employment_types]

  // [START company_name]

  /**
   * Search on company name.
   */
  public static void companyNameSearch(List<String> companyNames) throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            .setUserId("HashedUserId") // Make sure to hash your userID
            .setSessionId("HashedSessionID") // Make sure to hash the sessionID
            .setDomain("www.google.com"); // Domain of the website where the search is conducted

    JobQuery jobQuery = new JobQuery().setCompanyNames(companyNames);

    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setMode("JOB_SEARCH"); // Set the search mode to a regular search

    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
  }
  // [END company_name]

  // [START date_range]

  /**
   * Search on date range.
   */
  public static void dateRangeSearch() throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            .setUserId("HashedUserId") // Make sure to hash your userID
            .setSessionId("HashedSessionID") // Make sure to hash the sessionID
            .setDomain("www.google.com"); // Domain of the website where the search is conducted

    JobQuery jobQuery = new JobQuery().setPublishDateRange("PAST_3_DAYS");

    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setMode("JOB_SEARCH"); // Set the search mode to a regular search

    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
  }
  // [END date_range]

  // [START tenant_jobs]

  /**
   * Search on tenant jobs.
   */
  public static void tenantJobsSearch() throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            .setUserId("HashedUserId") // Make sure to hash your userID
            .setSessionId("HashedSessionID") // Make sure to hash the sessionID
            .setDomain("www.google.com"); // Domain of the website where the search is conducted

    JobQuery jobQuery = new JobQuery().setTenantJobOnly(true);

    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setMode("JOB_SEARCH"); // Set the search mode to a regular search

    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
  }
  // [END tenant_jobs]

  // [START language_code]

  /**
   * Search on language codes.
   */
  public static void languageCodeSearch() throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            .setUserId("HashedUserId") // Make sure to hash your userID
            .setSessionId("HashedSessionID") // Make sure to hash the sessionID
            .setDomain("www.google.com"); // Domain of the website where the search is conducted

    JobQuery jobQuery = new JobQuery().setLanguageCodes(Arrays.asList("pt-BR", "en-US"));

    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setMode("JOB_SEARCH"); // Set the search mode to a regular search

    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
  }
  // [END language_code]

  // [START company_display_name]

  /**
   * Search on company display name.
   */
  public static void companyDisplayNameSearch() throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            .setUserId("HashedUserId") // Make sure to hash your userID
            .setSessionId("HashedSessionID") // Make sure to hash the sessionID
            .setDomain("www.google.com"); // Domain of the website where the search is conducted

    JobQuery jobQuery =
        new JobQuery().setCompanyDisplayNames(Arrays.asList("Brown Group", "Blue Co", "Pink Inc"));

    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setMode("JOB_SEARCH"); // Set the search mode to a regular search

    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
  }
  // [END company_display_name]

  // [START compensation]

  /**
   * Search on compensation.
   */
  public static void compensationSearch() throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            .setUserId("HashedUserId") // Make sure to hash your userID
            .setSessionId("HashedSessionID") // Make sure to hash the sessionID
            .setDomain("www.google.com"); // Domain of the website where the search is conducted

    // Search jobs that pay between 10.50 and 15 USD per hour
    JobQuery jobQuery =
        new JobQuery()
            .setCompensationFilter(
                new CompensationFilter()
                    .setType("UNIT_AND_AMOUNT")
                    .setUnits(Arrays.asList("HOURLY"))
                    .setRange(
                        new CompensationRange()
                            .setMax(new Money().setCurrencyCode("USD").setUnits(15L))
                            .setMin(
                                new Money()
                                    .setCurrencyCode("USD")
                                    .setUnits(10L)
                                    .setNanos(500000000))));

    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setMode("JOB_SEARCH"); // Set the search mode to a regular search

    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
  }
  // [END compensation]

  public static void main(String... args) throws Exception {
    categoryFilterSearch();
    dateRangeSearch();
    employmentTypesSearch();
    tenantJobsSearch();
    companyDisplayNameSearch();
    compensationSearch();
    languageCodeSearch();

    Company company = CompanyAndJobCrudSample
        .createCompany(CompanyAndJobCrudSample.generateCompany());
    Job job = CompanyAndJobCrudSample
        .createJob(CompanyAndJobCrudSample.generateJobWithRequiredFields(company.getName()));
    companyNameSearch(Arrays.asList(company.getName()));
    CompanyAndJobCrudSample.deleteJob(job.getName());
    CompanyAndJobCrudSample.deleteCompany(company.getName());
  }
}

