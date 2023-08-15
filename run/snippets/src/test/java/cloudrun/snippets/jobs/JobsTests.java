/*
 * Copyright 2023 Google LLC
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

package cloudrun.snippets.jobs;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import com.google.cloud.run.v2.JobName;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JobsTests {

  private static final String project = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String location = "us-central1";
  private static final String imageUrl = "us-docker.pkg.dev/cloudrun/container/hello";
  private static String job;
  private final PrintStream originalOut = System.out;
  private ByteArrayOutputStream output;

  @BeforeClass
  public static void before() {
    job = "test-job-" + UUID.randomUUID().toString().substring(0, 30);
  }

  @Before
  public void beforeEach() {
    output = new ByteArrayOutputStream();
    System.setOut(new PrintStream(output));
    System.setErr(new PrintStream(output));
  }

  @After
  public void afterEach() {
    System.setOut(originalOut);
    output.reset();
  }

  @Test
  public void a_createJob() throws IOException, InterruptedException, ExecutionException {
    CreateJob.createJob(project, location, job, imageUrl);
    assertThat(output.toString()).contains(JobName.of(project, location, job).toString());
  }

  @Test
  public void b_getJob() throws IOException {
    GetJob.getJob(project, location, job);
    assertThat(output.toString()).contains(JobName.of(project, location, job).toString());
  }

  @Test
  public void c_listJobs() throws IOException {
    ListJobs.listJobs(project, location);
    assertThat(output.toString()).contains(JobName.of(project, location, job).toString());
  }

  @Test
  public void d_updateJob() throws IOException, InterruptedException, ExecutionException {
    UpdateJob.updateJob(project, location, job);
    assertThat(output.toString()).contains(JobName.of(project, location, job).toString());
  }

  @Test
  public void e_runJob() throws IOException, InterruptedException, ExecutionException {
    RunJob.runJob(project, location, job);
  }

//   @Test
//   public void e_iamPolicy() throws IOException {
//     // Set Policy
//     String member1 = "user:akitsch@google.com";
//     SetIamPolicy.setIamPolicy(project, location, job, member1, "roles/run.invoker");
//     assertThat(output.toString()).contains(member1);
//     // Get Policy
//     GetIamPolicy.getIamPolicy(project, location, job);
//     assertThat(output.toString()).contains("Role:");
//     // Update Policy
//     String member2 = "user:foo@domain.com";
//     UpdateIamPolicy.updateIamPolicy(project, location, job, member2, "roles/run.invoker");
//     assertThat(output.toString()).contains(String.format("[%s, %s]",member1, member2));
//   }

  @Test
  public void f_deleteJob() throws IOException, InterruptedException, ExecutionException {
    DeleteJob.deleteJob(project, location, job);
    assertThat(output.toString()).contains(JobName.of(project, location, job).toString());
  }
}
