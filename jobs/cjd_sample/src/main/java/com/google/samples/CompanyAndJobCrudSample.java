package com.google.samples;

import com.google.api.services.jobs.v2.JobService;
import com.google.api.services.jobs.v2.model.Company;
import com.google.api.services.jobs.v2.model.CreateJobRequest;
import com.google.api.services.jobs.v2.model.CustomAttribute;
import com.google.api.services.jobs.v2.model.Job;
import com.google.api.services.jobs.v2.model.StringValues;
import com.google.api.services.jobs.v2.model.UpdateJobRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Company and Job CRUD samples.
 */
public final class CompanyAndJobCrudSample {

  private static JobService jobService = JobServiceUtils.getJobService();

  // [START basic_company]

  /**
   * Generate a company
   */
  public static Company generateCompany() throws IOException {
    // distributor company id should be a unique Id in your system.
    String distributorCompanyId = "company:" + String.valueOf(new Random().nextLong());

    Company company =
        new Company()
            .setDisplayName("Google")
            .setHqLocation("1600 Amphitheatre Parkway Mountain View, CA 94043")
            .setDistributorCompanyId(distributorCompanyId);
    System.out.println("Company generated: " + company);
    return company;
  }
  // [END basic_company]

  // [START create_company]

  /**
   * Create a company.
   */
  public static Company createCompany(Company companyToBeCreated) throws IOException {
    Company companyCreated = jobService.companies().create(companyToBeCreated).execute();
    System.out.println("Company created: " + companyCreated);
    return companyCreated;
  }
  // [END create_company]

  // [START get_company]

  /**
   * Get a company.
   */
  public static Company getCompany(String companyName) throws IOException {
    Company companyExisted = jobService.companies().get(companyName).execute();
    System.out.println("Company existed: " + companyExisted);
    return companyExisted;
  }
  // [END get_company]

  // [START update_company]

  /**
   * Updates a company.
   */
  public static Company updateCompany(String companyName, Company companyToBeUpdated)
      throws IOException {
    Company companyUpdated = jobService.companies().patch(companyName, companyToBeUpdated)
        .execute();
    System.out.println("Company updated: " + companyUpdated);
    return companyUpdated;
  }
  // [END update_company]

  // [START delete_company]

  /**
   * Delete a company.
   */
  public static void deleteCompany(String companyName) throws IOException {
    jobService.companies().delete(companyName).execute();
    System.out.println("Company deleted");
  }
  // [END delete_company]

  // [START basic_job]

  /**
   * Generate a basic job with given companyName.
   */
  public static Job generateJobWithRequiredFields(String companyName) throws IOException {
    // requisition id should be a unique Id in your system.
    String requisitionId = "jobWithRequiredFields:" + String.valueOf(new Random().nextLong());

    Job job =
        new Job()
            .setRequisitionId(requisitionId)
            .setJobTitle("Software Engineer")
            .setCompanyName(companyName)
            .setApplicationUrls(Arrays.asList("http://careers.google.com"))
            .setDescription("Design, develop, test, deploy, maintain and improve software.");
    System.out.println("Job generated: " + job);
    return job;
  }
  // [END basic_job]

  // [START custom_attribute_job]

  /**
   * Generate a job with a custom attribute.
   */
  public static Job generateJobWithACustomAttribute(String companyName) throws IOException {
    // requisition id should be a unique Id in your system.
    String requisitionId = "jobWithACustomAttribute:" + String.valueOf(new Random().nextLong());

    // Constructs custom attributes map
    Map<String, CustomAttribute> customAttributes = new HashMap<>();
    customAttributes.put(
        "some_field_name",
        new CustomAttribute()
            .setStringValues(new StringValues().setValues(Arrays.asList("value1")))
            .setFilterable(true));

    // Creates job with custom attributes
    Job job =
        new Job()
            .setCompanyName(companyName)
            .setRequisitionId(requisitionId)
            .setJobTitle("Software Engineer")
            .setApplicationUrls(Arrays.asList("http://careers.google.com"))
            .setDescription("Design, develop, test, deploy, maintain and improve software.")
            .setCustomAttributes(customAttributes);
    System.out.println("Job generated: " + job);
    return job;
  }
  // [END custom_attribute_job]

  // [START create_job]

  /**
   * Create a job.
   */
  public static Job createJob(Job jobToBeCreated) throws IOException {
    CreateJobRequest createJobRequest = new CreateJobRequest().setJob(jobToBeCreated);
    Job jobCreated = jobService.jobs().create(createJobRequest).execute();
    System.out.println("Job created: " + jobCreated);
    return jobCreated;
  }
  // [END create_job]

  // [START get_job]

  /**
   * Get a job.
   */
  public static Job getJob(String jobName) throws IOException {
    Job jobExisted = jobService.jobs().get(jobName).execute();
    System.out.println("Job existed: " + jobExisted);
    return jobExisted;
  }
  // [END get_job]

  // [START update_job]

  /**
   * Update a job.
   */
  public static Job updateJob(String jobName, Job jobToBeUpdated) throws IOException {
    UpdateJobRequest updateJobRequest = new UpdateJobRequest().setJob(jobToBeUpdated);
    Job jobUpdated = jobService.jobs().patch(jobName, updateJobRequest).execute();
    System.out.println("Job updated: " + jobUpdated);
    return jobUpdated;
  }
  // [END update_job]

  // [START delete_job]

  /**
   * Delete a job.
   */
  public static void deleteJob(String jobName) throws IOException {
    jobService.jobs().delete(jobName).execute();
    System.out.println("Job deleted");
  }
  // [END delete_job]

  public static void main(String... args) throws Exception {
    Company companyToBeCreated = generateCompany();
    Company companyCreated = createCompany(companyToBeCreated);
    String companyName = companyCreated.getName();
    getCompany(companyName);
    Company companyToBeUpdated = companyCreated.setWebsite("https://elgoog.im/");
    updateCompany(companyName, companyToBeUpdated);

    Job jobToBeCreated = generateJobWithRequiredFields(companyName);
    Job jobCreated = createJob(jobToBeCreated);
    String jobName = jobCreated.getName();
    getJob(jobName);
    Job jobToBeUpdated = jobCreated.setDescription("Updated dummy description");
    updateJob(jobName, jobToBeUpdated);
    deleteJob(jobName);

    Job jobToBeCreated1 = generateJobWithACustomAttribute(companyName);
    Job jobCreated1 = createJob(jobToBeCreated1);
    String jobName1 = jobCreated1.getName();
    getJob(jobName1);
    Job jobToBeUpdated1 = jobCreated1.setDescription("Updated dummy description");
    updateJob(jobName1, jobToBeUpdated1);
    deleteJob(jobName1);

    deleteCompany(companyName);
  }
}
