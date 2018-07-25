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

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.jobs.v3.CloudTalentSolution;
import com.google.api.services.jobs.v3.CloudTalentSolution.Projects.Complete;
import com.google.api.services.jobs.v3.model.Company;
import com.google.api.services.jobs.v3.model.CompleteQueryResponse;
import com.google.api.services.jobs.v3.model.Job;
import java.io.IOException;
import java.util.Collections;

/**
 * The samples in this file introduced how to do the auto complete, including:
 *
 * - Default auto complete (on both company display name and job title)
 *
 * - Auto complete on job title only
 */
public final class AutoCompleteSample {

  // [Start setup]

  private static final JsonFactory JSON_FACTORY = new JacksonFactory();
  private static final NetHttpTransport NET_HTTP_TRANSPORT = new NetHttpTransport();
  private static final String SCOPES = "https://www.googleapis.com/auth/jobs";
  private static final String DEFAULT_PROJECT_ID =
      "projects/" + System.getenv("GOOGLE_CLOUD_PROJECT");

  private static CloudTalentSolution talentSolutionClient = createTalentSolutionClient(
      generateCredential());

  private static CloudTalentSolution createTalentSolutionClient(GoogleCredential credential) {
    String url = "https://integ-jobs.googleapis.com";
    return new CloudTalentSolution.Builder(
        NET_HTTP_TRANSPORT, JSON_FACTORY, setHttpTimeout(credential))
        .setApplicationName("JobServiceClientSamples")
        .setRootUrl(url)
        .build();
  }

  private static GoogleCredential generateCredential() {
    try {
      // Credentials could be downloaded after creating service account
      // set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable, for example:
      // export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your/key.json
      return GoogleCredential
          .getApplicationDefault(NET_HTTP_TRANSPORT, JSON_FACTORY)
          .createScoped(Collections.singleton(SCOPES));
    } catch (Exception e) {
      System.out.print("Error in generating credential");
      throw new RuntimeException(e);
    }
  }

  private static HttpRequestInitializer setHttpTimeout(
      final HttpRequestInitializer requestInitializer) {
    return request -> {
      requestInitializer.initialize(request);
      request.setHeaders(new HttpHeaders().set("X-GFE-SSL", "yes"));
      request.setConnectTimeout(1 * 60000); // 1 minute connect timeout
      request.setReadTimeout(1 * 60000); // 1 minute read timeout
    };
  }

  public static CloudTalentSolution getTalentSolutionClient() {
    return talentSolutionClient;
  }

  // [END setup]

  //[START auto_complete_job_title]

  /**
   * Auto completes job titles within given companyName.
   */
  public static void jobTitleAutoComplete(String companyName, String query)
      throws IOException {

    Complete complete =
        talentSolutionClient
            .projects()
            .complete(DEFAULT_PROJECT_ID)
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
  // [END auto_complete_default]

  /**
   * Auto completes job titles within given companyName.
   */
  public static void defaultAutoComplete(String companyName, String query)
      throws IOException {
    Complete complete =
        talentSolutionClient
            .projects()
            .complete(DEFAULT_PROJECT_ID)
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
        .setTitle("Software engineer");
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
