/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package cloudrun.snippets.executions;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import com.google.cloud.run.v2.Execution;
import com.google.cloud.run.v2.JobName;
import com.google.cloud.run.v2.ExecutionsClient.ListExecutionsPagedResponse;
import cloudrun.snippets.jobs.CreateJob;
import cloudrun.snippets.jobs.DeleteJob;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExecutionsTests {

    private static final String project = System.getenv("GOOGLE_CLOUD_PROJECT");
    private static final String location = "us-central1";
    private static final String imageUrl = "us-docker.pkg.dev/cloudrun/container/hello";
    private static String job;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream output;

    @BeforeClass
    public static void before() throws IOException, InterruptedException, ExecutionException {
        job = "test-job-" + UUID.randomUUID().toString().substring(0, 30);
        CreateJob.createJob(project, location, job, imageUrl);
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

    @AfterClass
    public static void after() throws IOException, InterruptedException, ExecutionException {
        DeleteJob.deleteJob(project, location, job);
    }

    @Test
    public void a_listExecutions() throws IOException, InterruptedException, ExecutionException {
        ListExecutions.listExecutions(project, location, job);
        assertThat(output.toString())
                .contains(JobName.of(project, location, job).toString());
    }

    @Test
    public void b_getExecutions() throws Exception {
        List<Execution> executions = ListExecutions.listExecutions(project, location, job);
        String executionName = executions.get(0).getName();
        int index = executionName.lastIndexOf("/");
        GetExecution.getExecution(project, location, job, executionName.substring(index + 1));
        assertThat(output.toString())
                .contains(JobName.of(project, location, job).toString());
    }

    @Test
    public void b_deleteExecutions() throws Exception {
        List<Execution> executions = ListExecutions.listExecutions(project, location, job);
        String executionName = executions.get(0).getName();
        int index = executionName.lastIndexOf("/");
        GetExecution.getExecution(project, location, job, executionName.substring(index + 1));
        assertThat(output.toString())
                .contains(JobName.of(project, location, job).toString());
    }
}
