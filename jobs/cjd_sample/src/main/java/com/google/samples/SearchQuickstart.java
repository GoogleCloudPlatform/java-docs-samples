package com.google.samples;

import com.google.api.services.jobs.v2.JobService;
import com.google.api.services.jobs.v2.JobService.V2.Complete;
import com.google.api.services.jobs.v2.model.CompleteQueryResponse;
import com.google.api.services.jobs.v2.model.CompletionResult;
import com.google.api.services.jobs.v2.model.CreateJobRequest;
import com.google.api.services.jobs.v2.model.HistogramFacets;
import com.google.api.services.jobs.v2.model.Job;
import com.google.api.services.jobs.v2.model.JobQuery;
import com.google.api.services.jobs.v2.model.MatchingJob;
import com.google.api.services.jobs.v2.model.RequestMetadata;
import com.google.api.services.jobs.v2.model.SearchJobsRequest;
import com.google.api.services.jobs.v2.model.SearchJobsResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Cloud Job Discovery Search QuickStart.
 *
 * Includes basic job search and histogram search.
 */
public final class SearchQuickstart {

  private static JobService jobService = JobServiceUtils.getJobService();

  // [START create_jobs]
  public static final Job JOB_SAMPLE_1 =
      new Job()
          .setApplicationInstruction("Apply to this job on www.careers.google.com. ")
          .setDescription(
              "Financial Analysts ensure that Google makes sound financial decisions. As a "
                  + "Financial Analyst, your work, whether it's modeling business scenarios or "
                  + "tracking performance metrics, is used by our leaders to make strategic "
                  + "company decisions. A master juggler working on multiple projects at a time, "
                  + "you are focused on the details while finding creative ways to take on big "
                  + "picture challenges.")
          .setRequisitionId("584632881")
          .setLocations(Arrays.asList("Palo Alto, CA"))
          .setJobTitle("Financial Analyst");

  // Below job matches the job category of ADMINISTRATIVE_AND_OFFICE.
  public static final Job JOB_SAMPLE_2 =
      new Job()
          .setApplicationInstruction("Apply to this job on www.careers.google.com. ")
          .setDescription(
              "We are seeking an energetic, friendly and ambitious professional to handle multiple "
                  + "administrative and customer service related responsibilities.")
          .setRequisitionId("584632882")
          .setLocations(Arrays.asList("Palo Alto, CA"))
          .setJobTitle("Receptionist");

  // Below job matches the job category of ADVERTISING_AND_MARKETING.
  public static final Job JOB_SAMPLE_3 =
      new Job()
          .setApplicationInstruction("Apply to this job on www.careers.google.com. ")
          .setDescription(
              "As a Marketing Manager, you'll create experiences and tentpole moments that bring "
                  + "to life the passion of the YouTube audience for our platform, and that build "
                  + "excitement, trust and advocacy among brand marketers and leaders. Working "
                  + "closely with cross-functional and global teams including Product, Sales, PR, "
                  + "Events and Research, you will make strategic decisions that change marketers’ "
                  + "perceptions and influence the industry as a whole. From uncovering new "
                  + "insights, to performing competitive analysis and developing external "
                  + "communications, you help position YouTube’s offering for this audience.")
          .setRequisitionId("584632883")
          .setLocations(Arrays.asList("Palo Alto, CA"))
          .setJobTitle("Marketing Manager");

  // Below job matches the job category of ANIMAL_CARE.
  public static final Job JOB_SAMPLE_4 =
      new Job()
          .setApplicationInstruction("Apply to this job on www.careers.google.com. ")
          .setDescription(
              "We're making things better for pets, people and the planet. This job is composed "
                  + "of a variety of different tasks that are covered by operational guidelines, "
                  + "and while individual judgment may occasionally be required in order to "
                  + "complete assigned tasks. ")
          .setRequisitionId("584632884")
          .setLocations(Arrays.asList("Palo Alto, CA"))
          .setJobTitle("Pet Care Manager");

  // Below job matches the job category of ART_FASHION_AND_DESIGN.
  public static final Job JOB_SAMPLE_5 =
      new Job()
          .setApplicationInstruction("Apply to this job on www.careers.google.com. ")
          .setDescription(
              "As a Graphic Designer, you will help create the visual elements (such as art, "
                  + "graphics, and photography) used in the production of marketing materials, "
                  + "design of products or packaging, creation of advertising/sales promotional "
                  + "materials, and in-store visual displays for a variety of clients.")
          .setRequisitionId("584632885")
          .setLocations(Arrays.asList("Palo Alto, CA"))
          .setJobTitle("Graphic Designer");

  // Below job matches the job category of BUSINESS_OPERATIONS.
  public static final Job JOB_SAMPLE_6 =
      new Job()
          .setApplicationInstruction("Apply to this job on www.careers.google.com. ")
          .setDescription(
              "The Business Strategy & Operations organization provides business critical insights "
                  + "using analytics, ensures cross functional alignment of goals and execution, "
                  + "and helps teams drive strategic partnerships and new initiatives forward. We "
                  + "stay focused on aligning the highest-level company priorities with strong "
                  + "day-to-day operations, and help evolve early stage ideas into future-growth "
                  + "initiatives.")
          .setRequisitionId("584632886")
          .setLocations(Arrays.asList("Palo Alto, CA"))
          .setJobTitle("Senior Business Analyst");

  // Below job matches the job category of CLEANING_AND_FACILITIES.
  public static final Job JOB_SAMPLE_7 =
      new Job()
          .setApplicationInstruction("Apply to this job on www.careers.google.com. ")
          .setDescription(
              "As a Room Attendant, your contribution helps ensure guests an enjoyable and "
                  + "comfortable stay. As Housekeeper, you will Clean, dust, polish and vacuum "
                  + "to make sure guest rooms and bathrooms meet standards.")
          .setRequisitionId("584632887")
          .setLocations(Arrays.asList("Palo Alto, CA"))
          .setJobTitle("Housekeeping Room Attendant");

  public static final List<Job> JOBS =
      Arrays.asList(
          JOB_SAMPLE_1,
          JOB_SAMPLE_2,
          JOB_SAMPLE_3,
          JOB_SAMPLE_4,
          JOB_SAMPLE_5,
          JOB_SAMPLE_6,
          JOB_SAMPLE_7);

  /**
   * Insert sample jobs with given companyName.
   */
  public static void insert(String companyName) throws IOException {
    for (Job job : JOBS) {
      job.setCompanyName(companyName);
      Job returnJob =
          jobService
              .jobs()
              .create(new CreateJobRequest().setDisableStreetAddressResolution(true).setJob(job))
              .execute();
      String jobName = returnJob.getName();
      System.out.println(jobName);
    }
  }
  //[END create_jobs]

  //[START basic_search]

  /**
   * Simple search jobs with keyword.
   */
  public static void basicSearcJobs() throws IOException {
    // Make sure to set the requestMetadata to reflect the unique jobb seeker. Obfuscate these fields.
    RequestMetadata requestMetadata =
        new RequestMetadata()
            .setUserId("HashedUserId") // Make sure to hash the userID
            .setSessionId("HashedSessionID") // Make sure to hash the sessionID
            .setDomain(
                "www.google.com"); // This is the domain of the website on which the search is
    // conducted

    // Perform a search for analyst  related jobs
    JobQuery jobQuery = new JobQuery().setQuery("analyst");

    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setQuery(jobQuery) // Set the actual search term as defined in the jobQurey
            .setMode("JOB_SEARCH"); // Set the search mode to a regular search

    SearchJobsResponse response = jobService.jobs().search(request).execute();

    if (response.containsKey("matchingJobs")) {
      for (MatchingJob match : response.getMatchingJobs()) {
        System.out.println(match.getJob().getJobTitle());
        System.out.println(match.getJob().getName());
        System.out.println(match.getJobSummary());
      }
    } else {
      System.out.println("No jobs for this search");
    }
  }
  //[END basic_search]

  //[START histogram_search]

  /**
   * Histogram search jobs with keyword.
   */
  public static void histogramSearchJobs() throws IOException {
    // Make sure to set the requestMetadata the same as the associated search request
    RequestMetadata requestMetadata =
        new RequestMetadata()
            .setUserId("HashedUserId") // Make sure to hash your userID
            .setSessionId("HashedSessionID") // Make sure to hash the sessionID
            .setDomain(
                "www.google.com"); // This is the domain of the website on which the search is
    // conducted

    HistogramFacets histogramFacets =
        new HistogramFacets().setSimpleHistogramFacets(Arrays.asList("CATEGORY", "CITY"));
    SearchJobsRequest request =
        new SearchJobsRequest()
            .setRequestMetadata(requestMetadata)
            .setMode("JOB_SEARCH")
            .setQuery(new JobQuery().setQuery("analyst"))
            .setHistogramFacets(histogramFacets);
    SearchJobsResponse response = jobService.jobs().search(request).execute();
    System.out.println(response.getHistogramResults());
  }
  //[END histogram_search]

  //[START auto_complete]

  /**
   * Auto completes job titles within given companyName.
   */
  public static void jobTitleAutoComplete(String companyName) throws IOException {
    Complete complete = jobService
        .v2()
        .complete()
        .setQuery("ana") // First few characters in a search for analyst (auto complete)
        .setLanguageCode("en-US")
        .setPageSize(10);
    if (companyName != null) {
      complete.setCompanyName(companyName);
    }

    CompleteQueryResponse results = complete.execute();

    for (CompletionResult res : results.getCompletionResults()) {
      System.out.println(res.getSuggestion());
    }
  }
  // [END auto_complete]

  public static void main(String... args) throws Exception {
    basicSearcJobs();
    histogramSearchJobs();
    jobTitleAutoComplete(null);
  }
}
