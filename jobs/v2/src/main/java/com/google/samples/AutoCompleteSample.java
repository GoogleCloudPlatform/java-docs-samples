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
import com.google.api.services.jobs.v2.JobService.V2.Complete;
import com.google.api.services.jobs.v2.model.Company;
import com.google.api.services.jobs.v2.model.CompleteQueryResponse;
import com.google.api.services.jobs.v2.model.Job;
import java.io.IOException;

/**
 * The samples in this file introduced how to do the auto complete, including:
 *
 * - Default auto complete (on both company display name and job title)
 *
 * - Auto complete on job title only
 */
public final class AutoCompleteSample {

  private static JobService jobService = JobServiceQuickstart.getJobService();

  //[START auto_complete_job_title]

  /**
   * Auto completes job titles within given companyName.
   */
  public static void jobTitleAutoComplete(String companyName, String query)
      throws IOException {
    Complete complete = jobService
        .v2()
        .complete()
        .setQuery(query)
        .setLanguageCode("en-US")
        .setType("JOB_TITLE")
        .setPageSize(10);
    if (companyName != null) {
      complete.setCompanyName(companyName);
    }

    CompleteQueryResponse results = complete.execute();

    System.out.println(results);
  }
  // [END auto_complete_job_title]

  // [START auto_complete_default]
  /**
   * Auto completes job titles within given companyName.
   */
  public static void defaultAutoComplete(String companyName, String query)
      throws IOException {
    Complete complete = jobService
        .v2()
        .complete()
        .setQuery(query)
        .setLanguageCode("en-US")
        .setPageSize(10);
    if (companyName != null) {
      complete.setCompanyName(companyName);
    }

    CompleteQueryResponse results = complete.execute();

    System.out.println(results);
  }
  // [END auto_complete_default]

  public static void main(String... args) throws Exception {
    Company companyToBeCreated = BasicCompanySample.generateCompany().setDisplayName("Google");
    String companyName = BasicCompanySample.createCompany(companyToBeCreated).getName();

    Job jobToBeCreated = BasicJobSample.generateJobWithRequiredFields(companyName)
        .setJobTitle("Software engineer");
    final String jobName = BasicJobSample.createJob(jobToBeCreated).getName();

    // Wait several seconds for post processing
    Thread.sleep(10000);
    defaultAutoComplete(companyName,"goo");
    defaultAutoComplete(companyName,"sof");
    jobTitleAutoComplete(companyName, "sof");

    BasicJobSample.deleteJob(jobName);
    BasicCompanySample.deleteCompany(companyName);
  }
}
