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
import com.google.api.services.jobs.v3.model.Company;
import com.google.api.services.jobs.v3.model.CreateCompanyRequest;
import com.google.api.services.jobs.v3.model.UpdateCompanyRequest;
import java.io.IOException;
import java.util.Random;

/**
 * This file contains the basic knowledge about company and job, including:
 *
 * - Construct a company with required fields
 *
 * - Create a company
 *
 * - Get a company
 *
 * - Update a company
 *
 * - Update a company with field mask
 *
 * - Delete a company
 */
public final class BasicCompanySample {

  private static final String DEFAULT_PROJECT_ID =
      "projects/" + System.getenv("GOOGLE_CLOUD_PROJECT");

  private static CloudTalentSolution talentSolutionClient = JobServiceQuickstart
      .getTalentSolutionClient();

  // [START basic_company]

  /**
   * Generate a company
   */
  public static Company generateCompany() {
    // distributor company id should be a unique Id in your system.
    String companyName =
        "company:" + String.valueOf(new Random().nextLong());

    Company company =
        new Company()
            .setDisplayName("Google")
            .setHeadquartersAddress("1600 Amphitheatre Parkway Mountain View, CA 94043")
            .setExternalId(companyName);
    System.out.println("Company generated: " + company);
    return company;
  }
  // [END basic_company]

  // [START create_company]

  /**
   * Create a company.
   */
  public static Company createCompany(Company companyToBeCreated) throws IOException {
    try {
      CreateCompanyRequest createCompanyRequest =
          new CreateCompanyRequest().setCompany(companyToBeCreated);
      Company companyCreated =
          talentSolutionClient.projects()
              .companies()
              .create(DEFAULT_PROJECT_ID, createCompanyRequest)
              .execute();
      System.out.println("Company created: " + companyCreated);
      return companyCreated;
    } catch (IOException e) {
      System.out.println("Got exception while creating company");
      throw e;
    }
  }
  // [END create_company]

  // [START get_company]

  /**
   * Get a company.
   */
  public static Company getCompany(String companyName) throws IOException {
    try {
      Company companyExisted =
          talentSolutionClient.projects().companies().get(companyName).execute();
      System.out.println("Company existed: " + companyExisted);
      return companyExisted;
    } catch (IOException e) {
      System.out.println("Got exception while getting company");
      throw e;
    }
  }
  // [END get_company]

  // [START update_company]

  /**
   * Updates a company.
   */
  public static Company updateCompany(String companyName, Company companyToBeUpdated)
      throws IOException {
    try {
      UpdateCompanyRequest updateCompanyRequest =
          new UpdateCompanyRequest().setCompany(companyToBeUpdated);

      Company companyUpdated =
          talentSolutionClient
              .projects()
              .companies()
              .patch(companyName, updateCompanyRequest)
              .execute();

      System.out.println("Company updated: " + companyUpdated);
      return companyUpdated;
    } catch (IOException e) {
      System.out.println("Got exception while updating company");
      throw e;
    }
  }
  // [END update_company]

  // [START update_company_with_field_mask]

  /**
   * Updates a company.
   */
  public static Company updateCompanyWithFieldMask(String companyName, String fieldMask,
      Company companyToBeUpdated)
      throws IOException {
    try {
      // String foo = String.format("?updateCompanyFields=%s",fieldMask);
      UpdateCompanyRequest updateCompanyRequest =
          new UpdateCompanyRequest().setUpdateMask(fieldMask).setCompany(companyToBeUpdated);

      Company companyUpdated =
          talentSolutionClient
              .projects()
              .companies()
              .patch(companyName, updateCompanyRequest)
              .execute();

      System.out.println("Company updated: " + companyUpdated);
      return companyUpdated;
    } catch (IOException e) {
      System.out.println("Got exception while updating company");
      throw e;
    }
  }
  // [END update_company_with_field_mask]

  // [START delete_company]

  /**
   * Delete a company.
   */
  public static void deleteCompany(String companyName) throws IOException {
    try {
      talentSolutionClient.projects().companies().delete(companyName).execute();
      System.out.println("Company deleted");
    } catch (IOException e) {
      System.out.println("Got exception while deleting company");
      throw e;
    }
  }
  // [END delete_company]

  public static void main(String... args) throws Exception {
    // Construct a company
    Company companyToBeCreated = generateCompany();

    // Create a company
    Company companyCreated = createCompany(companyToBeCreated);

    // Get a company
    String companyName = companyCreated.getName();
    getCompany(companyName);

    // Update a company
    Company companyToBeUpdated = companyCreated
        .setCareerSiteUri("https://elgoog.im/");
    updateCompany(companyName, companyToBeUpdated);

    // Update a company with field mask
    updateCompanyWithFieldMask(companyName, "displayName",
        new Company().setDisplayName("changedTitle")
            .setName(companyCreated.getName()));

    // Delete a company
    deleteCompany(companyName);
  }
}
