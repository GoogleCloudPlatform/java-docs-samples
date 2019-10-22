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
import com.google.api.services.jobs.v3.model.ApplicationInfo;
import com.google.api.services.jobs.v3.model.Company;
import com.google.api.services.jobs.v3.model.CustomAttribute;
import com.google.api.services.jobs.v3.model.Job;
import com.google.api.services.jobs.v3.model.JobQuery;
import com.google.api.services.jobs.v3.model.RequestMetadata;
import com.google.api.services.jobs.v3.model.SearchJobsRequest;
import com.google.api.services.jobs.v3.model.SearchJobsResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

;

/**
 * This file contains the samples about CustomAttribute, including:
 *
 * - Construct a Job with CustomAttribute
 *
 * - Search Job with CustomAttributeFilter
 */
public final class CustomAttributeSample {

  private static final String DEFAULT_PROJECT_ID =
      "projects/" + System.getenv("GOOGLE_CLOUD_PROJECT");

  private static CloudTalentSolution talentSolutionClient = JobServiceQuickstart
      .getTalentSolutionClient();

  // [START custom_attribute_job]

  /**
   * Generate a job with a custom attribute.
   */
  public static Job generateJobWithACustomAttribute(String companyName) {
    // requisition id should be a unique Id in your system.
    String requisitionId =
        "jobWithACustomAttribute:" + String.valueOf(new Random().nextLong());
    ApplicationInfo applicationInfo =
        new ApplicationInfo().setUris(Arrays.asList("http://careers.google.com"));

    // Constructs custom attributes map
    Map<String, CustomAttribute> customAttributes = new HashMap<>();
    customAttributes.put(
        "someFieldName1",
        new CustomAttribute()
            .setStringValues(Arrays.asList("value1"))
            .setFilterable(Boolean.TRUE));
    customAttributes
        .put("someFieldName2",
            new CustomAttribute().setLongValues(Arrays.asList(256L)).setFilterable(true));

    // Creates job with custom attributes
    Job job =
        new Job()
            .setCompanyName(companyName)
            .setRequisitionId(requisitionId)
            .setTitle("Software Engineer")
            .setApplicationInfo(applicationInfo)
            .setDescription("Design, develop, test, deploy, maintain and improve software.")
            .setCustomAttributes(customAttributes);
    System.out.println("Job generated: " + job);
    return job;
  }
  // [END custom_attribute_job]

  // [START custom_attribute_filter_string_value]

  /**
   * CustomAttributeFilter on String value CustomAttribute
   */
  public static void filtersOnStringValueCustomAttribute() throws IOException {
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

    String customAttributeFilter = "NOT EMPTY(someFieldName1)";
    JobQuery jobQuery = new JobQuery()
        .setCustomAttributeFilter(customAttributeFilter);

    SearchJobsRequest searchJobsRequest =
        new SearchJobsRequest()
            .setJobQuery(jobQuery)
            .setRequestMetadata(requestMetadata)
            .setJobView("JOB_VIEW_FULL");
    SearchJobsResponse response =
        talentSolutionClient
            .projects()
            .jobs()
            .search(DEFAULT_PROJECT_ID, searchJobsRequest)
            .execute();
    System.out.println(response);
  }
  // [END custom_attribute_filter_string_value]

  // [START custom_attribute_filter_long_value]

  /**
   * CustomAttributeFilter on Long value CustomAttribute
   */
  public static void filtersOnLongValueCustomAttribute() throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            // Make sure to hash your userID
            .setUserId("HashedUserId")
            // Make sure to hash the sessionID
            .setSessionId("HashedSessionID")
            // Domain of the website where the search is conducted
            .setDomain("www.google.com");

    String customAttributeFilter = "(255 <= someFieldName2) AND (someFieldName2 <= 257)";
    JobQuery jobQuery = new JobQuery()
        .setCustomAttributeFilter(customAttributeFilter);

    SearchJobsRequest searchJobsRequest =
        new SearchJobsRequest()
            .setJobQuery(jobQuery)
            .setJobView("JOB_VIEW_FULL")
            .setRequestMetadata(requestMetadata);

    SearchJobsResponse response =
        talentSolutionClient
            .projects()
            .jobs()
            .search(DEFAULT_PROJECT_ID, searchJobsRequest)
            .execute();
    System.out.println(response);
  }
  // [END custom_attribute_filter_long_value]

  // [START custom_attribute_filter_multi_attributes]

  /**
   * CustomAttributeFilter on multiple CustomAttributes
   */
  public static void filtersOnMultiCustomAttributes() throws IOException {
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

    String customAttributeFilter = "(someFieldName1 = \"value1\") "
        + "AND ((255 <= someFieldName2) OR (someFieldName2 <= 213))";
    JobQuery jobQuery = new JobQuery()
        .setCustomAttributeFilter(customAttributeFilter);

    SearchJobsRequest searchJobsRequest =
        new SearchJobsRequest()
            .setJobQuery(jobQuery)
            .setRequestMetadata(requestMetadata)
            .setJobView("JOB_VIEW_FULL");
    SearchJobsResponse response =
        talentSolutionClient
            .projects()
            .jobs()
            .search(DEFAULT_PROJECT_ID, searchJobsRequest)
            .execute();
    System.out.println(response);
  }
  // [END custom_attribute_filter_multi_attributes]

  public static void main(String... args) throws Exception {
    Company companyToBeCreated = BasicCompanySample.generateCompany();
    String companyName = BasicCompanySample.createCompany(companyToBeCreated).getName();

    Job jobToBeCreated = generateJobWithACustomAttribute(companyName);
    final String jobName = BasicJobSample.createJob(jobToBeCreated).getName();

    // Wait several seconds for post processing
    Thread.sleep(10000);
    filtersOnStringValueCustomAttribute();
    filtersOnLongValueCustomAttribute();
    filtersOnMultiCustomAttributes();

    BasicJobSample.deleteJob(jobName);
    BasicCompanySample.deleteCompany(companyName);
  }
}
