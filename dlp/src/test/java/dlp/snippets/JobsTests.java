/*
 * Copyright 2020 Google Inc.
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

package dlp.snippets;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class JobsTests {

    private static final Pattern JOB_ID_PATTERN = Pattern.compile("projects/.*/dlpJobs/i-\\d+");
    private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
    private static final String GCS_PATH = "gs://" + PROJECT_ID + "/dlp";
    private static final String PUB_SUB_TOPIC_ID = "dlp-tests";
    private static final String PUB_SUB_SUBSCRIPTION_ID = "dlp-test";
    private ByteArrayOutputStream bout;

    private static void requireEnvVar(String varName) {
        assertNotNull(
                String.format("Environment variable '%s' must be set to perform these tests.", varName),
                System.getenv(varName));
    }

    @BeforeClass
    public static void checkRequirements() {
        requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
        requireEnvVar("GOOGLE_CLOUD_PROJECT");
    }

    @Before
    public void setUp() throws Exception {
        bout = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bout));

        // Ensure that there is at least one job to list
        InspectGcsFile.inspectGcsFile(PROJECT_ID,
                GCS_PATH,
                PUB_SUB_TOPIC_ID,
                PUB_SUB_SUBSCRIPTION_ID);
    }


    @After
    public void tearDown() {
        System.setOut(null);
        bout.reset();
    }

    @Test
    public void testListJobs() throws Exception {
        // Call listJobs to print out a list of jobIds
        JobsList.listJobs(PROJECT_ID);
        String output = bout.toString();

        // Check that the output contains jobIds
        Matcher matcher = JOB_ID_PATTERN.matcher(bout.toString());
        assertTrue("List must contain results.", matcher.find());
    }

    @Test
    public void testDeleteJobs() throws Exception {
        // Get a list of JobIds, and extract one to delete
        JobsList.listJobs(PROJECT_ID);
        String output = bout.toString();
        Matcher matcher = JOB_ID_PATTERN.matcher(bout.toString());
        assertTrue("List must contain results.", matcher.find());

        // Extract just the ID
        String jobId = matcher.group(0).split("/")[3];
        bout.reset();

        // Delete the Job
        JobsDelete.deleteJobs(PROJECT_ID, jobId);
        output = bout.toString();
        assertTrue(output.contains("Job deleted successfully."));
    }
}
