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

package aiplatform;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertNotNull;

import com.google.cloud.aiplatform.v1.CustomJobName;
import com.google.cloud.aiplatform.v1.JobServiceClient;
import com.google.cloud.aiplatform.v1.JobServiceSettings;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

public class CreateCustomJobSampleTest {

  @Rule public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(5);

  private static final String PROJECT = System.getenv("UCAIP_PROJECT_ID");
  private static final String CONTAINER_IMAGE_URI =
      "gcr.io/ucaip-sample-tests/ucaip-training-test:latest";
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private PrintStream originalPrintStream;
  private String customJobId;

  private static void requireEnvVar(String varName) {
    String errorMessage =
        String.format("Environment variable '%s' is required to perform these tests.", varName);
    assertNotNull(errorMessage, System.getenv(varName));
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("UCAIP_PROJECT_ID");
  }

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    originalPrintStream = System.out;
    System.setOut(out);
  }

  @After
  public void tearDown()
      throws InterruptedException, ExecutionException, IOException, TimeoutException {
    JobServiceSettings settings =
        JobServiceSettings.newBuilder()
            .setEndpoint("us-central1-aiplatform.googleapis.com:443")
            .build();

    try (JobServiceClient client = JobServiceClient.create(settings)) {
      // Cancel custom job
      String location = "us-central1";
      CustomJobName customJobName = CustomJobName.of(PROJECT, location, customJobId);
      client.cancelCustomJob(customJobName);

      TimeUnit.MINUTES.sleep(2);

      // Delete the created job
      client.deleteCustomJobAsync(customJobName);
      System.out.flush();
      System.setOut(originalPrintStream);
    }
  }

  @Test
  public void testCreateCustomJobSample() throws IOException {
    String customJobDisplayName =
        String.format(
            "temp_custom_job_display_name_%s",
            UUID.randomUUID().toString().replaceAll("-", "_").substring(0, 26));

    CreateCustomJobSample.createCustomJobSample(PROJECT, customJobDisplayName, CONTAINER_IMAGE_URI);

    String got = bout.toString();
    assertThat(got).contains(customJobDisplayName);
    assertThat(got).contains("response:");
    customJobId = got.split("Name: ")[1].split("customJobs/")[1].split("\n")[0];
  }
}
