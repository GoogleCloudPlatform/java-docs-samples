package com.google.samples;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.jobs.v2.JobService;
import com.google.api.services.jobs.v2.model.Company;
import com.google.api.services.jobs.v2.model.CreateJobRequest;
import com.google.api.services.jobs.v2.model.Empty;
import com.google.api.services.jobs.v2.model.Job;
import com.google.api.services.jobs.v2.model.UpdateJobRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Batch operations for job create, update and delete.
 */
public final class BatchOperationSample {

  private static JobService jobService = JobServiceUtils.getJobService();

  // [START batch_operation]

  /**
   * Batch operations for job create, update and delete.
   */
  public static void batchOperations() throws IOException {
    String companyName = jobService.companies().create(
        new Company().setDisplayName("BatchOperationSample")
            .setDistributorCompanyId(
                "BatchOperationSample:" + String.valueOf(new Random().nextLong()))).execute()
        .getName();

    List<Job> createdJobs = new ArrayList<>();

    // Callback for batch create
    JsonBatchCallback<Job> createCallback =
        new JsonBatchCallback<Job>() {
          @Override
          public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
            System.out.println("Create Error Message: " + e.getMessage());
          }

          @Override
          public void onSuccess(Job response, HttpHeaders responseHeaders) {
            System.out.println("Create Job: " + response);
            createdJobs.add(response);
          }
        };

    Job softwareEngineerJob =
        new Job()
            .setCompanyName(companyName)
            .setRequisitionId("123456")
            .setJobTitle("Software Engineer")
            .setApplicationUrls(Arrays.asList("http://careers.google.com"))
            .setDescription("Design, develop, test, deploy, maintain and improve software.");
    Job hardwareEngineerJob =
        new Job()
            .setCompanyName(companyName)
            .setRequisitionId("1234567")
            .setJobTitle("Hardware Engineer")
            .setApplicationUrls(Arrays.asList("http://careers.google.com"))
            .setDescription(
                "Design prototype PCBs or modify existing board designs "
                    + "to prototype new features or functions.");

    // Creates batch request
    BatchRequest batchCreate = jobService.batch();

    // Queues create job request
    jobService
        .jobs()
        .create(new CreateJobRequest().setJob(softwareEngineerJob))
        .queue(batchCreate, createCallback);
    jobService
        .jobs()
        .create(new CreateJobRequest().setJob(hardwareEngineerJob))
        .queue(batchCreate, createCallback);

    // Executes batch request
    batchCreate.execute();

    // Batch update jobs
    List<Job> updatedJobs = new ArrayList<>();

    JsonBatchCallback<Job> updateCallback =
        new JsonBatchCallback<Job>() {
          @Override
          public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
            System.out.println("Update Error Message: " + e.getMessage());
          }

          @Override
          public void onSuccess(Job job, HttpHeaders responseHeaders) throws IOException {
            System.out.println("Update Job: " + job);
            updatedJobs.add(job);
          }
        };

    BatchRequest batchUpdate = jobService.batch();
    for (Job toBeUpdated : createdJobs) {
      toBeUpdated.setJobTitle("Engineer in Mountain View");
      jobService
          .jobs()
          .patch(
              toBeUpdated.getName(),
              new UpdateJobRequest().setJob(toBeUpdated).setUpdateJobFields("jobTitle"))
          .queue(batchUpdate, updateCallback);
    }
    batchUpdate.execute();

    // Batch delete jobs
    BatchRequest batchDelete = jobService.batch();
    for (Job job : updatedJobs) {
      jobService
          .jobs()
          .delete(job.getName())
          .queue(
              batchDelete,
              new JsonBatchCallback<Empty>() {
                @Override
                public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders)
                    throws IOException {
                  System.out.println("Delete Error Message: " + e.getMessage());
                }

                @Override
                public void onSuccess(Empty empty, HttpHeaders responseHeaders)
                    throws IOException {
                  System.out.println("Company deleted");
                }
              });
    }
    batchDelete.execute();

    jobService.companies().delete(companyName).execute();
  }
  // [END batch_operation]

  public static void main(String... args) throws Exception {
    batchOperations();
  }
}
