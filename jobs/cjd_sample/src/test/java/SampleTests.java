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

import static com.google.common.truth.Truth.assertThat;

import com.google.samples.AutoCompleteSample;
import com.google.samples.BasicCompanySample;
import com.google.samples.BasicJobSample;
import com.google.samples.BatchOperationSample;
import com.google.samples.CommuteSearchSample;
import com.google.samples.CustomAttributeSample;
import com.google.samples.CustomFieldSample;
import com.google.samples.EmailAlertSearchSample;
import com.google.samples.FeaturedJobsSearchSample;
import com.google.samples.GeneralSearchSample;
import com.google.samples.HistogramSample;
import com.google.samples.LocationSearchSample;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SampleTests {

  private ByteArrayOutputStream bout;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);
    System.setOut(out);
  }

  @Test
  public void runAllSamples() throws Exception {
    AutoCompleteSample.main();
    assertThat(bout.toString()).containsMatch(
        ".*completionResults.*\"suggestion\":\"Google\",\"type\":\"COMPANY_NAME\"}.*\n"
            + ".*completionResults.*\"suggestion\":\"Software Engineer\",\"type\":\"JOB_TITLE\".*\n"
            + ".*completionResults.*\"suggestion\":\"Software Engineer\",\"type\":\"JOB_TITLE\".*\n"
    );
    bout.reset();

    BasicCompanySample.main();
    assertThat(bout.toString()).containsMatch(
        ".*Company generated:.*\n"
            + ".*Company created:.*\n"
            + ".*Company existed:.*\n"
            + ".*Company updated:.*elgoog.*\n"
            + ".*Company updated:.*changedTitle.*\n"
            + ".*Company deleted.*\n"
    );
    bout.reset();

    BasicJobSample.main();
    assertThat(bout.toString()).containsMatch(
        ".*Job generated:.*\n"
            + ".*Job created:.*\n"
            + ".*Job existed:.*\n"
            + ".*Job updated:.*changedDescription.*\n"
            + ".*Job updated:.*changedJobTitle.*\n"
            + ".*Job deleted.*\n"
    );
    bout.reset();

    BatchOperationSample.main();
    assertThat(bout.toString()).containsMatch(".*"
        + "Company generated:.*\nCompany created:.*\n"
        + "Create Job:.*\nCreate Job:.*\n"
        + "Update Job:.*Engineer in Mountain View.*\nUpdate Job:.*Engineer in Mountain View.*\n"
        + "Job deleted.*\nJob deleted.*\n"
        + "Company deleted.*\n"
    );
    bout.reset();

    CommuteSearchSample.main();
    assertThat(bout.toString())
        .containsMatch(".*appliedCommuteFilter.*1600 Amphitheatre Pkwy.*");
    bout.reset();

    CustomAttributeSample.main();
    assertThat(bout.toString())
        .containsMatch(
            ".*Job created:.*jobWithACustomAttribute.*\n"
                + ".*matchingJobs.*jobWithACustomAttribute.*\n"
                + ".*matchingJobs.*jobWithACustomAttribute.*\n"
                + ".*matchingJobs.*jobWithACustomAttribute.*\n");
    bout.reset();

    CustomFieldSample.main();
    assertThat(bout.toString())
        .containsMatch(
            ".*Job created:.*jobWithACustomField.*\n"
                + ".*matchingJobs.*jobWithACustomField.*\n"
                + ".*matchingJobs.*jobWithACustomField.*\n"
                + ".*matchingJobs.*jobWithACustomField.*\n");
    bout.reset();

    EmailAlertSearchSample.main();
    assertThat(bout.toString()).contains("matchingJobs");
    bout.reset();

    GeneralSearchSample.main();
    assertThat(bout.toString())
        .containsMatch(
            ".*matchingJobs.*\n"
                + ".*matchingJobs.*\n"
                + ".*matchingJobs.*\n"
                + ".*matchingJobs.*\n"
                + ".*matchingJobs.*\n"
                + ".*matchingJobs.*\n"
                + ".*matchingJobs.*\n"
                + ".*matchingJobs.*\n");
    bout.reset();

    FeaturedJobsSearchSample.main();
    assertThat(bout.toString()).contains("matchingJobs");
    bout.reset();

    HistogramSample.main();
    assertThat(bout.toString()).contains("COMPANY_DISPLAY_NAME");
    assertThat(bout.toString()).contains("someFieldName1");
    bout.reset();

    LocationSearchSample.main();
    assertThat(bout.toString()).containsMatch(
        ".*appliedJobLocationFilters.*matchingJobs.*\n"
            + ".*appliedJobLocationFilters.*matchingJobs.*\n"
            + ".*appliedJobLocationFilters.*matchingJobs.*\n"
            + ".*appliedJobLocationFilters.*matchingJobs.*\n"
            + ".*appliedJobLocationFilters.*matchingJobs.*\n"
    );
    bout.reset();
  }
}
