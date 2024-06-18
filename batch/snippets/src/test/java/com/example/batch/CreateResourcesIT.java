package com.example.batch;

import static com.google.common.truth.Truth.assertWithMessage;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RunWith(JUnit4.class)
public class CreateResourcesIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION = "us-central1";

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
            .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeClass
  public static void setUp() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @AfterClass
  public static void cleanUp()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    DeleteJob.deleteJob(PROJECT_ID, REGION, "");
  }
}
