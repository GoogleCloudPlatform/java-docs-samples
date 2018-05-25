<<<<<<< HEAD
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

=======
>>>>>>> cjd samples
package com.google.samples;

import com.google.api.services.jobs.v2.JobService;
import com.google.api.services.jobs.v2.model.Company;
import com.google.api.services.jobs.v2.model.CustomAttribute;
import com.google.api.services.jobs.v2.model.Job;
import com.google.api.services.jobs.v2.model.JobQuery;
<<<<<<< HEAD
import com.google.api.services.jobs.v2.model.RequestMetadata;
import com.google.api.services.jobs.v2.model.SearchJobsRequest;
import com.google.api.services.jobs.v2.model.SearchJobsResponse;
=======
>>>>>>> cjd samples
import com.google.api.services.jobs.v2.model.StringValues;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
<<<<<<< HEAD
import java.util.Random;

/**
 * This file contains the samples about CustomAttribute, including:
 *
 * - Construct a Job with CustomAttribute
 *
 * - Search Job with CustomAttributeFilter
=======

/**
 * Cloud Job Discovery Custom Attribute Samples.
>>>>>>> cjd samples
 */
public final class CustomAttributeSample {

  private static JobService jobService = JobServiceQuickstart.getJobService();

<<<<<<< HEAD
  // [START custom_attribute_job]

  /**
   * Generate a job with a custom attribute.
   */
  public static Job generateJobWithACustomAttribute(String companyName) {
    // requisition id should be a unique Id in your system.
    String requisitionId =
        "jobWithACustomAttribute:" + String.valueOf(new Random().nextLong());

    // Constructs custom attributes map
    Map<String, CustomAttribute> customAttributes = new HashMap<>();
    customAttributes.put(
        "someFieldName1",
        new CustomAttribute()
            .setStringValues(
                new StringValues().setValues(Arrays.asList("value1")))
            .setFilterable(true));
    customAttributes
        .put("someFieldName2", new CustomAttribute().setLongValue(256L).setFilterable(true));

    // Creates job with custom attributes
    Job job =
        new Job()
            .setCompanyName(companyName)
            .setRequisitionId(requisitionId)
            .setJobTitle("Software Engineer")
            .setApplicationUrls(Arrays.asList("http://careers.google.com"))
            .setDescription(
                "Design, develop, test, deploy, maintain and improve software.")
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
            .setQuery(jobQuery)
            .setRequestMetadata(requestMetadata)
            .setJobView("FULL");
    SearchJobsResponse response = jobService.jobs().search(searchJobsRequest).execute();
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
            .setDomain(
                "www.google.com");

    String customAttributeFilter = "(255 <= someFieldName2) AND (someFieldName2 <= 257)";
    JobQuery jobQuery = new JobQuery()
        .setCustomAttributeFilter(customAttributeFilter);

    SearchJobsRequest searchJobsRequest =
        new SearchJobsRequest()
            .setQuery(jobQuery)
            .setRequestMetadata(requestMetadata)
            .setJobView("FULL");
    SearchJobsResponse response = jobService.jobs().search(searchJobsRequest).execute();
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
            .setQuery(jobQuery)
            .setRequestMetadata(requestMetadata)
            .setJobView("FULL");
    SearchJobsResponse response = jobService.jobs().search(searchJobsRequest).execute();
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
=======
  // [START multiple_custom_attributes_job]

  /**
   * Creates a new job with advanced custom fields (both numeric and string).
   */
  public static Job createJobWithNumericAndStringCustomFields(String companyName)
      throws IOException {
    Map<String, CustomAttribute> customAttributeMap = new HashMap<>();
    customAttributeMap.put(
        "skills",
        new CustomAttribute()
            .setStringValues(
                new StringValues()
                    .setValues(
                        Arrays.asList("team management", "personal organization and risk control")))
            .setFilterable(true));
    customAttributeMap.put(
        "target_bonus_rate", new CustomAttribute().setLongValue(15L).setFilterable(true));
    customAttributeMap.put(
        "additional_info",
        new CustomAttribute()
            .setStringValues(
                new StringValues()
                    .setValues(Arrays.asList("some additional information not searchable"))));
    Job job =
        new Job()
            .setCompanyName(companyName)
            .setRequisitionId("123456")
            .setJobTitle("Software Engineer")
            .setApplicationUrls(Arrays.asList("http://careers.google.com"))
            .setDescription("Design, develop, test, deploy, maintain and improve software.")
            .setCustomAttributes(customAttributeMap);
    CreateJobRequest createJobRequest = new CreateJobRequest().setJob(job);
    Job createdJob = jobService.jobs().create(createJobRequest).execute();
    System.out.println(createdJob.getName());
    return createdJob;
  }
  // [END multiple_custom_attributes_job]

  /**
   * Example filters on custom attributes.
   */
  public static void filtersOnCustomAttributes() throws IOException {

    // [START custom_attribute_example_1]
    String salaryCustomAttributeFilter = "(100000 <= salary) AND (salary <= 200000)";
    JobQuery salaryJobQuery = new JobQuery().setCustomAttributeFilter(salaryCustomAttributeFilter);
    // [END custom_attribute_example_1]

    // [START custom_attribute_example_2]
    String sponsoredJobFilter = "(NOT EMPTY(is_sponsored_job)) AND (is_sponsored_job = \"true\")";
    JobQuery sponsorJobQuery = new JobQuery().setCustomAttributeFilter(sponsoredJobFilter);
    // [END custom_attribute_example_2]

    // [START custom_attribute_example_3]
    String langudageWithAccessControlFlag =
        "      ( "
            + "    (text = \"lang1\") "
            + "    AND (text2 = \"lang2\") "
            + "    AND (text3 = \"true\") "
            + "    AND (ACF1 = \"languages\") "
            + "    AND (ACF2 = \"language list\")"
            + ") "
            + "OR "
            + "(ACF3 = \"true\" OR ACF3 = \"false\")";
    JobQuery languageAcfQuery =
        new JobQuery().setCustomAttributeFilter(langudageWithAccessControlFlag);
    // [END custom_attribute_example_3]

    // [START custom_attribute_example_4]
    String filter =
        "      ("
            + "    ((10 < target_bonus_rate) AND (target_bonus_rate < 20)) "
            + "    OR "
            + "    (skills = \"Team Management\") OR (skills = \"team management\" )"
            + " ) "
            + " AND "
            + " (NOT EMPTY(visa_required)";
    JobQuery crossFieldFilterQuery =
        new JobQuery().setQuery("Program Manager").setCustomAttributeFilter(filter);
    // [END custom_attribute_example_4]
  }

  /**
   * Filters pn custom fields.
   */
  public static void filtersOnCustomFields(String companyName) throws IOException {

    // [START custom_field_job]
    // Creates a new job with existing id based custom fields (only string allowed)
    Map<String, CustomField> customFieldMap = new HashMap<>();
    customFieldMap.put(
        "1", new CustomField().setValues(Arrays.asList("team management", "risk control")));
    Job job =
        new Job()
            .setCompanyName(companyName)
            .setRequisitionId("234567")
            .setJobTitle("Technical Program Manager")
            .setApplicationUrls(Arrays.asList("http://careers.google.com"))
            .setDescription(
                "As a Technical Program Manager at Google, you lead complex, multi-disciplinary "
                    + "engineering projects using your engineering expertise.")
            .setFilterableCustomFields(customFieldMap);
    CreateJobRequest createJobRequest = new CreateJobRequest().setJob(job);
    Job createdJob = jobService.jobs().create(createJobRequest).execute();
    System.out.println(createdJob.getName());
    // [END custom_field_job]

    // [START custom_field_example_1]
    // Creates AND filter on existing id based custom fields
    Map<String, CustomFieldFilter> customFieldAndFilterMap = new HashMap<>();
    customFieldAndFilterMap.put(
        "1",
        new CustomFieldFilter()
            .setType("AND")
            .setQueries(Arrays.asList("team management", "risk control")));
    JobFilters customFieldAndFilter =
        new JobFilters().setQuery("Program Manager").setCustomFieldFilters(customFieldAndFilterMap);
    // [END custom_field_example_1]

    // [START custom_field_example_2]
    // Creates OR filter on existing id based custom fields
    Map<String, CustomFieldFilter> customFieldOrFilterMap = new HashMap<>();
    customFieldOrFilterMap.put(
        "1",
        new CustomFieldFilter()
            .setType("OR")
            .setQueries(Arrays.asList("team management", "risk control")));
    JobFilters customFieldOrFilter =
        new JobFilters().setQuery("Program Manager").setCustomFieldFilters(customFieldOrFilterMap);
    // [END custom_field_example_2]

    // [START custom_field_example_3]
    // Creates composite filter on existing id based custom fields
    Map<String, CustomFieldFilter> customFieldCompositeFilterMap = new HashMap<>();
    customFieldCompositeFilterMap.put(
        "1",
        new CustomFieldFilter()
            .setType("OR")
            .setQueries(Arrays.asList("team management", "risk control")));
    customFieldCompositeFilterMap.put(
        "2", new CustomFieldFilter().setType("NOT").setQueries(Arrays.asList("blabla")));
    JobFilters customFieldCompositeFilter =
        new JobFilters()
            .setQuery("Program Manager")
            .setCustomFieldFilters(customFieldCompositeFilterMap);
    // [END custom_field_example_3]

    CompanyAndJobCrudSample.deleteJob(createdJob.getName());
  }

  public static void main(String... args) throws Exception {
    Company company = CompanyAndJobCrudSample
        .createCompany(CompanyAndJobCrudSample.generateCompany());
    Job job = createJobWithNumericAndStringCustomFields(company.getName());

    filtersOnCustomFields(company.getName());
    filtersOnCustomAttributes();

    CompanyAndJobCrudSample.deleteJob(job.getName());
    CompanyAndJobCrudSample.deleteCompany(company.getName());
>>>>>>> cjd samples
  }
}
