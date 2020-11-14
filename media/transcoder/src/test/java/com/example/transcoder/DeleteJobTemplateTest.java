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

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import com.example.transcoder.CreateJobTemplate;
import com.example.transcoder.DeleteJobTemplate;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DeleteJobTemplateTest {

  private static final String LOCATION = "us-central1";
  private static final String TEMPLATE_ID = "my-job-template";
  private static String PROJECT_ID;
  private static String PROJECT_NUMBER;
  private final PrintStream originalOut = System.out;
  private ByteArrayOutputStream bout;

  private static String requireEnvVar(String varName) {
    String varValue = System.getenv(varName);
    assertNotNull(
        String.format("Environment variable '%s' is required to perform these tests.", varName));
    return varValue;
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    PROJECT_ID = requireEnvVar("GOOGLE_CLOUD_PROJECT");
    PROJECT_NUMBER = requireEnvVar("GOOGLE_CLOUD_PROJECT_NUMBER");
  }

  @Before
  public void beforeTest() throws IOException {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));

    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));

    try {
      DeleteJobTemplate.deleteJobTemplate(PROJECT_ID, LOCATION, TEMPLATE_ID);
    } catch (GoogleJsonResponseException gjre) {
    }
    try {
      CreateJobTemplate.createJobTemplate(PROJECT_ID, LOCATION, TEMPLATE_ID);
    } catch (GoogleJsonResponseException gjre) {
    }
    bout.reset();
  }

  @Test
  public void test_DeleteJobTemplate() throws IOException {
    try {
      DeleteJobTemplate.deleteJobTemplate(PROJECT_ID, LOCATION, TEMPLATE_ID);
    } catch (GoogleJsonResponseException gjre) {
    }
    String output = bout.toString();
    assertThat(output, containsString("Deleted job template"));
    bout.reset();
  }

  @After
  public void tearDown() throws IOException {
    System.setOut(originalOut);
    bout.reset();
  }
}
