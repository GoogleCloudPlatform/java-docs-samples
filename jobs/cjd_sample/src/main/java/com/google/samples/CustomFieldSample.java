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
import com.google.api.services.jobs.v2.model.CreateJobRequest;
import com.google.api.services.jobs.v2.model.CustomAttribute;
import com.google.api.services.jobs.v2.model.CustomField;
import com.google.api.services.jobs.v2.model.CustomFieldFilter;
import com.google.api.services.jobs.v2.model.Job;
import com.google.api.services.jobs.v2.model.JobFilters;
import com.google.api.services.jobs.v2.model.JobQuery;
import com.google.api.services.jobs.v2.model.RequestMetadata;
import com.google.api.services.jobs.v2.model.SearchJobsRequest;
import com.google.api.services.jobs.v2.model.SearchJobsResponse;
import com.google.api.services.jobs.v2.model.StringValues;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * This file contains the samples about CustomField, including:
 *
 * - Construct a Job with CustomField
 *
 * - Search Job with CustomFieldFilter
 *
 * Note: CustomField is similar to CustomAttribute but it is about to be deprecated. The key for
 * CustomField could only be 1~20 in string and the value could only be string.
 */
public final class CustomFieldSample {

  private static JobService jobService = JobServiceUtils.getJobService();

  // [START custom_field_job]

  /**
   * Generate a job with a custom attribute.
   */
  public static Job generateJobWithACustomAttribute(String companyName) {
    // requisition id should be a unique Id in your system.
    String requisitionId =
        "jobWithACustomField:" + String.valueOf(new Random().nextLong());

    // Constructs custom field map
    Map<String, CustomField> customFieldMap = new HashMap<>();
    customFieldMap.put(
        "1", new CustomField()
            .setValues(Arrays.asList("value1ForField1", "value2ForField1")));
    customFieldMap.put("2", new CustomField()
        .setValues(Arrays.asList("value1ForField2", "value2ForField2")));
    customFieldMap.put("3", new CustomField()
        .setValues(Arrays.asList("value1ForField3", "value2ForField3")));

    // Creates job with custom attributes
    Job job =
        new Job()
            .setCompanyName(companyName)
            .setRequisitionId(requisitionId)
            .setJobTitle("Software Engineer")
            .setApplicationUrls(Arrays.asList("http://careers.google.com"))
            .setDescription(
                "Design, develop, test, deploy, maintain and improve software.")
            .setFilterableCustomFields(customFieldMap);
    System.out.println("Job generated: " + job);
    return job;
  }
  // [END custom_field_job]

  // [START custom_field_filter_and]

  /**
   * CustomFieldFilter with type 'AND'
   */
  public static void filtersOnCustomFieldWithTypeAnd() throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            // Make sure to hash your userID
            .setUserId("HashedUserId")
            // Make sure to hash the sessionID
            .setSessionId("HashedSessionID")
            // Domain of the website where the search is conducted
            .setDomain(
                "www.google.com");

    Map<String, CustomFieldFilter> customFieldAndFilterMap = new HashMap<>();
    customFieldAndFilterMap.put(
        "1", new CustomFieldFilter()
            .setType("AND")
            .setQueries(Arrays.asList("value1ForField1", "value2ForField1")));

    // JobFilters is deprecated and replaced by JobQuery.
    // But customFieldFilters could only be set in JobFilters
    JobFilters jobFilters = new JobFilters().setCustomFieldFilters(customFieldAndFilterMap);

    SearchJobsRequest searchJobsRequest =
        new SearchJobsRequest()
            .setFilters(jobFilters)
            .setRequestMetadata(requestMetadata)
            .setJobView("FULL");
    SearchJobsResponse response = jobService.jobs().search(searchJobsRequest).execute();
    System.out.println(response);
  }
  // [END custom_field_filter_and]

  // [START custom_field_filter_or]

  /**
   * CustomFieldFilter with type 'OR'
   */
  public static void filtersOnCustomFieldWithTypeOr() throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            // Make sure to hash your userID
            .setUserId("HashedUserId")
            // Make sure to hash the sessionID
            .setSessionId("HashedSessionID")
            // Domain of the website where the search is conducted
            .setDomain(
                "www.google.com");

    Map<String, CustomFieldFilter> customFieldAndFilterMap = new HashMap<>();
    customFieldAndFilterMap.put(
        "1", new CustomFieldFilter()
            .setType("OR")
            .setQueries(Arrays.asList("value1ForField1", "value2ForField2")));

    // JobFilters is deprecated and replaced by JobQuery.
    // But customFieldFilters could only be set in JobFilters
    JobFilters jobFilters = new JobFilters().setCustomFieldFilters(customFieldAndFilterMap);

    SearchJobsRequest searchJobsRequest =
        new SearchJobsRequest()
            .setFilters(jobFilters)
            .setRequestMetadata(requestMetadata)
            .setJobView("FULL");
    SearchJobsResponse response = jobService.jobs().search(searchJobsRequest).execute();
    System.out.println(response);
  }
  // [END custom_field_filter_or]

  // [START custom_field_filter_not]

  /**
   * CustomFieldFilter with type 'NOT'
   */
  public static void filtersOnCustomFieldWithTypeNot() throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            // Make sure to hash your userID
            .setUserId("HashedUserId")
            // Make sure to hash the sessionID
            .setSessionId("HashedSessionID")
            // Domain of the website where the search is conducted
            .setDomain(
                "www.google.com");

    Map<String, CustomFieldFilter> customFieldAndFilterMap = new HashMap<>();
    customFieldAndFilterMap.put(
        "1", new CustomFieldFilter()
            .setType("NOT")
            .setQueries(Arrays.asList("value2ForField2")));
    customFieldAndFilterMap.put(
        "2", new CustomFieldFilter()
            .setType("AND")
            .setQueries(Arrays.asList("value2ForField2")));

    // JobFilters is deprecated and replaced by JobQuery.
    // But customFieldFilters could only be set in JobFilters
    JobFilters jobFilters = new JobFilters().setCustomFieldFilters(customFieldAndFilterMap);

    SearchJobsRequest searchJobsRequest =
        new SearchJobsRequest()
            .setFilters(jobFilters)
            .setRequestMetadata(requestMetadata)
            .setJobView("FULL");
    SearchJobsResponse response = jobService.jobs().search(searchJobsRequest).execute();
    System.out.println(response);
  }
  // [END custom_field_filter_not]

  public static void main(String... args) throws Exception {
    Company companyToBeCreated = BasicCompanySample.generateCompany();
    String companyName = BasicCompanySample.createCompany(companyToBeCreated).getName();

    Job jobToBeCreated = generateJobWithACustomAttribute(companyName);
    final String jobName = BasicJobSample.createJob(jobToBeCreated).getName();

    // Wait several seconds for post processing
    Thread.sleep(10000);
    filtersOnCustomFieldWithTypeAnd();
    filtersOnCustomFieldWithTypeOr();
    filtersOnCustomFieldWithTypeNot();

    BasicJobSample.deleteJob(jobName);
    BasicCompanySample.deleteCompany(companyName);
  }
}
