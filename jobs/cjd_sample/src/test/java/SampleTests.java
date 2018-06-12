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

import com.google.samples.BatchOperationSample;
import com.google.samples.CommuteSearchSample;
import com.google.samples.CompanyAndJobCrudSample;
import com.google.samples.CustomAttributeSample;
import com.google.samples.EmailAlertSearchSample;
import com.google.samples.HistogramSample;
import com.google.samples.LocationSearchSample;
import com.google.samples.SearchBasicSamples;
import com.google.samples.SearchFeaturedJobsSample;
import com.google.samples.SearchFiltersSample;
import com.google.samples.SearchQuickstart;
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
    BatchOperationSample.main();
    assertThat(bout.toString()).containsMatch(".*"
        + "Company generated:.*\nCompany created:.*\n"
        + "Create Job:.*\nCreate Job:.*\n"
        + "Update Job:.*\nUpdate Job:.*\n"
        + "Job deleted.*\nJob deleted.*\n"
        + "Company deleted.*\n"
    );
    bout.reset();

    CommuteSearchSample.main();
    assertThat(bout.toString()).contains("appliedCommuteFilter");
    bout.reset();

    CompanyAndJobCrudSample.main();
    assertThat(bout.toString()).containsMatch(
        ".*"
            + "Company generated:.*\nCompany created:.*\nCompany existed:.*\nCompany updated:.*\n"
            + "Job generated:.*\nJob created:.*\nJob existed:.*\nJob updated:.*\nJob deleted.*\n"
            + "Job generated:.*\nJob created:.*\nJob existed:.*\nJob updated:.*\nJob deleted.*\n"
            + "Company deleted.*\n"
    );
    bout.reset();

    // TODO(xinyunh):need to improve the sample.
    CustomAttributeSample.main();

    EmailAlertSearchSample.main();
    assertThat(bout.toString()).contains("matchingJobs");
    bout.reset();

    HistogramSample.main();
    assertThat(bout.toString()).contains("histogramResults");
    bout.reset();

    LocationSearchSample.main();
    assertThat(bout.toString()).containsMatch(
        ".*appliedJobLocationFilters.*\n"
            + ".*appliedJobLocationFilters.*\n"
            + ".*appliedJobLocationFilters.*\n"
            + ".*appliedJobLocationFilters.*\n"
            + ".*appliedJobLocationFilters.*\n"
    );
    bout.reset();

    // TODO(xinyunh):need to improve the sample.
    SearchBasicSamples.main();

    // TODO(xinyunh):need to improve the sample.
    SearchFeaturedJobsSample.main();

    // TODO(xinyunh):need to improve the sample.
    SearchFiltersSample.main();

    // TODO(xinyunh):need to improve the sample.
    SearchQuickstart.main();
  }
}
