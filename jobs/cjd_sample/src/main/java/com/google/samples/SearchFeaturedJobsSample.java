package com.google.samples;

import com.google.api.services.jobs.v2.JobService;
import com.google.api.services.jobs.v2.model.Company;
import com.google.api.services.jobs.v2.model.CreateJobRequest;
import com.google.api.services.jobs.v2.model.Job;
import com.google.api.services.jobs.v2.model.JobQuery;
import com.google.api.services.jobs.v2.model.RequestMetadata;
import com.google.api.services.jobs.v2.model.SearchJobsRequest;
import com.google.api.services.jobs.v2.model.SearchJobsResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

/**
 * Search featured jobs example.
 */
public final class SearchFeaturedJobsSample {

  private static JobService jobService = JobServiceUtils.getJobService();

  // [START create_featured_job]

  /**
   * Creates a job as featured.
   */
  public static Job createFeaturedJob(String companyName) throws IOException {
    // requisition id should be a unique Id in your system.
    String requisitionId = "jobWithRequiredFields:" + String.valueOf(new Random().nextLong());

    Job job =
        new Job()
            .setRequisitionId(requisitionId)
            .setJobTitle("Software Engineer")
            .setCompanyName(companyName)
            .setApplicationUrls(Arrays.asList("http://careers.google.com"))
            .setDescription("Design, develop, test, deploy, maintain and improve software.")
            .setPromotionValue(2);
    CreateJobRequest createJobRequest = new CreateJobRequest().setJob(job);
    Job createdJob = jobService.jobs().create(createJobRequest).execute();
    System.out.println(createdJob.getName());
    return createdJob;
  }
  // [END create_featured_job]

  // [START search_featured_job]

  /**
   * Searches featured jobs.
   */
  public static void searchFeaturedJobs() throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            .setUserId("HashedUserId") // Make sure to hash the userID
            .setSessionId("HashedSessionID") // Make sure to hash the sessionID
            .setDomain("www.google.com"); // Domain of the website where the search is conducted

    JobQuery jobQuery = new JobQuery().setQuery("Software Engineer");

    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setMode("FEATURED_JOB_SEARCH"); // Set the search mode to a featured search
    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response);
  }
  // [END search_featured_job]

  public static void main(String... args) throws Exception {
    Company company = CompanyAndJobCrudSample
        .createCompany(CompanyAndJobCrudSample.generateCompany());
    Job job = createFeaturedJob(company.getName());
    searchFeaturedJobs();
    CompanyAndJobCrudSample.deleteJob(job.getName());
    CompanyAndJobCrudSample.deleteCompany(company.getName());
  }
}
