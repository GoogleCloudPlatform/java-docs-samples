/*
 * Copyright 2020 Google LLC
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

import static com.google.common.truth.Truth.assertThat;

import com.example.jobs.JobSearchCreateCompany;
import com.example.jobs.JobSearchCreateTenant;
import com.example.jobs.JobSearchDeleteCompany;
import com.example.jobs.JobSearchDeleteTenant;
import com.example.jobs.JobSearchGetCompany;
import com.example.jobs.JobSearchListCompanies;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JobSearchListGetCompanyTest {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String TENANT_EXT_ID = "EXTERNAL_TEMP_TENANT_ID";
  private static final String COMPANY_DISPLAY_NAME = "GOOGLE_DEVREL";
  private static final String COMPANY_EXT_ID = "COMPANY_EXTERNAL_ID";

  private String tenantId;
  private String companyId;

  private ByteArrayOutputStream bout;
  private PrintStream out;

  @Before
  public void setUp() throws IOException {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
    // create a tenant for job and company
    JobSearchCreateTenant.createTenant(PROJECT_ID, TENANT_EXT_ID);

    String got = bout.toString();
    assertThat(got).contains("Created Tenant");

    tenantId = extractLastId(got.split("\n")[1]);

    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);

    // create a company
    JobSearchCreateCompany.createCompany(
        PROJECT_ID, tenantId, COMPANY_DISPLAY_NAME, COMPANY_EXT_ID);

    got = bout.toString();
    assertThat(got).contains("Created Company");

    companyId = extractLastId(got.split("\n")[1]);

    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() throws IOException {
    JobSearchDeleteCompany.deleteCompany(PROJECT_ID, tenantId, companyId);
    String got = bout.toString();
    assertThat(got).contains("Deleted company");

    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);

    JobSearchDeleteTenant.deleteTenant(PROJECT_ID, tenantId);
    got = bout.toString();
    assertThat(got).contains("Deleted Tenant.");

    System.setOut(null);
  }

  @Test
  public void testListGetCompanies() throws IOException {

    // list companies.
    JobSearchListCompanies.listCompanies(PROJECT_ID, tenantId);
    String got = bout.toString();
    assertThat(got).contains("Company Name:");

    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);

    // get a company.
    JobSearchGetCompany.getCompany(PROJECT_ID, tenantId, companyId);
    got = bout.toString();

    assertThat(got).contains("Company name:");
    assertThat(got).contains("Display name:");
  }

  // Helper method for getting the last id from the full path.
  public static String extractLastId(String fullPath) {
    if (fullPath == null || fullPath.length() < 1 || !fullPath.contains("/")) {
      throw new IllegalArgumentException("Not valid path");
    }

    String[] parts = fullPath.split("/");
    return parts[parts.length - 1];
  }
}
