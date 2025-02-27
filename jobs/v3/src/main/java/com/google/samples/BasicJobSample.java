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
import com.google.api.services.jobs.v3.model.CreateJobRequest;
import com.google.api.services.jobs.v3.model.Job;
import com.google.api.services.jobs.v3.model.UpdateJobRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

/**
 * This file contains the basic knowledge about job, including:
 *
 * - Construct a job with required fields
 *
 * - Create a job
 *
 * - Get a job
 *
 * - Update a job
 *
 * - Update a job with field mask
 *
 * - Delete a job
 */
public final class BasicJobSample {

  private static final String DEFAULT_PROJECT_ID =
      "projects/" + System.getenv("GOOGLE_CLOUD_PROJECT");

  private static CloudTalentSolution talentSolutionClient =
      JobServiceQuickstart.getTalentSolutionClient();

  // [START job_basic_job]
  // [START basic_job]

  /** Generate a basic job with given companyName. */
  public static Job generateJobWithRequiredFields(String companyName) {
    // requisition id should be a unique Id in your system.
    String requisitionId = "jobWithRequiredFields:" + String.valueOf(new Random().nextLong());
    ApplicationInfo applicationInfo =
        new ApplicationInfo().setUris(Arrays.asList("http://careers.google.com"));

    Job job =
        new Job()
            .setRequisitionId(requisitionId)
            .setTitle("Software Engineer")
            .setCompanyName(companyName)
            .setApplicationInfo(applicationInfo)
            .setDescription("Design, develop, test, deploy, maintain and improve software.");
    System.out.println("Job generated: " + job);
    return job;
  }
  // [END basic_job]
  // [END job_basic_job]

  // [START job_create_job]

  /** Create a job. */
  public static Job createJob(Job jobToBeCreated) throws IOException {
    try {
      CreateJobRequest createJobRequest = new CreateJobRequest().setJob(jobToBeCreated);

      Job jobCreated =
          talentSolutionClient
              .projects()
              .jobs()
              .create(DEFAULT_PROJECT_ID, createJobRequest)
              .execute();
      System.out.println("Job created: " + jobCreated);
      return jobCreated;
    } catch (IOException e) {
      System.out.println("Got exception while creating job");
      throw e;
    }
  }
  // [END job_create_job]

  // [START job_get_job]

  /** Get a job. */
  public static Job getJob(String jobName) throws IOException {
    try {
      Job jobExisted = talentSolutionClient.projects().jobs().get(jobName).execute();
      System.out.println("Job existed: " + jobExisted);
      return jobExisted;
    } catch (IOException e) {
      System.out.println("Got exception while getting job");
      throw e;
    }
  }
  // [END job_get_job]

  // [START update_job]
  // [START job_update_job]

  /** Update a job. */
  public static Job updateJob(String jobName, Job jobToBeUpdated) throws IOException {
    try {
      UpdateJobRequest updateJobRequest = new UpdateJobRequest().setJob(jobToBeUpdated);
      Job jobUpdated =
          talentSolutionClient.projects().jobs().patch(jobName, updateJobRequest).execute();
      System.out.println("Job updated: " + jobUpdated);
      return jobUpdated;
    } catch (IOException e) {
      System.out.println("Got exception while updating job");
      throw e;
    }
  }

  // [END update_job]
  // [END job_update_job]

  // [START job_update_job_with_field_mask]

  /** Update a job. */
  public static Job updateJobWithFieldMask(String jobName, String fieldMask, Job jobToBeUpdated)
      throws IOException {
    try {
      UpdateJobRequest updateJobRequest =
          new UpdateJobRequest().setUpdateMask(fieldMask).setJob(jobToBeUpdated);
      Job jobUpdated =
          talentSolutionClient.projects().jobs().patch(jobName, updateJobRequest).execute();
      System.out.println("Job updated: " + jobUpdated);
      return jobUpdated;
    } catch (IOException e) {
      System.out.println("Got exception while updating job");
      throw e;
    }
  }
  // [END job_update_job_with_field_mask]

  // [START job_delete_job]
  // [START delete_job]

  /** Delete a job. */
  public static void deleteJob(String jobName) throws IOException {
    try {
      talentSolutionClient.projects().jobs().delete(jobName).execute();
      System.out.println("Job deleted");
    } catch (IOException e) {
      System.out.println("Got exception while deleting job");
      throw e;
    }
  }
  // [END delete_job]
  // [END job_delete_job]

  public static void main(String... args) throws Exception {
    // Create a company before creating jobs
    Company companyToBeCreated = BasicCompanySample.generateCompany();
    Company companyCreated = BasicCompanySample.createCompany(companyToBeCreated);
    String companyName = companyCreated.getName();

    // Construct a job
    Job jobToBeCreated = generateJobWithRequiredFields(companyName);

    // Create a job
    Job jobCreated = createJob(jobToBeCreated);

    // Get a job
    String jobName = jobCreated.getName();
    getJob(jobName);

    // Update a job
    Job jobToBeUpdated = jobCreated.setDescription("changedDescription");
    updateJob(jobName, jobToBeUpdated);

    // Update a job with field mask
    updateJobWithFieldMask(jobName, "title", new Job().setTitle("changedJobTitle"));

    // Delete a job
    deleteJob(jobName);

    // Delete company only after cleaning all jobs under this company
    BasicCompanySample.deleteCompany(companyName);
  }
}
