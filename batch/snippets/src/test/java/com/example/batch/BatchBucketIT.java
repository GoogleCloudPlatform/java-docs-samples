// Copyright 2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.batch;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.batch.v1.Job;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageClass;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.MissingResourceException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BatchBucketIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION = "us-central1";
  private static String SCRIPT_JOB_NAME;
  private static final int MAX_ATTEMPT_COUNT = 3;
  private static final int INITIAL_BACKOFF_MILLIS = 120000; // 2 minutes
  private static String BUCKET_NAME;
  private ByteArrayOutputStream stdOut;

  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(
      MAX_ATTEMPT_COUNT,
      INITIAL_BACKOFF_MILLIS);

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName))
        .isNotEmpty();
  }

  @BeforeClass
  public static void setUp()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    try (PrintStream out = System.out) {
      ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
      System.setOut(new PrintStream(stdOut));
      requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
      requireEnvVar("GOOGLE_CLOUD_PROJECT");

      String uuid = String.valueOf(UUID.randomUUID());
      SCRIPT_JOB_NAME = "test-job-script-" + uuid;
      BUCKET_NAME = "test-bucket-" + uuid;

      createBucket(BUCKET_NAME);
      TimeUnit.SECONDS.sleep(10);

      stdOut.close();
      System.setOut(out);
    }
  }

  @AfterClass
  public static void cleanup()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    try (PrintStream out = System.out) {
      ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
      System.setOut(new PrintStream(stdOut));

      // Delete bucket.
      Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
      Bucket bucket = storage.get(BUCKET_NAME);
      for (Blob blob : storage.list(bucket.getName()).iterateAll()) {
        storage.delete(blob.getBlobId());
      }
      storage.delete(bucket.getName());
      System.out.println("Bucket " + bucket.getName() + " was deleted");

      // Delete job.
      DeleteJob.deleteJob(PROJECT_ID, REGION, SCRIPT_JOB_NAME);

      stdOut.close();
      System.setOut(out);
    }
  }

  private static void createBucket(String bucketName) {
    Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    StorageClass storageClass = StorageClass.COLDLINE;
    String location = "US";
    storage.create(
        BucketInfo.newBuilder(bucketName)
            .setStorageClass(storageClass)
            .setLocation(location)
            .build());
  }

  @Before
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @After
  public void afterEach() {
    stdOut = null;
    System.setOut(null);
  }

  @Test
  public void testBucketJob() throws IOException, ExecutionException, InterruptedException,
      MissingResourceException, TimeoutException {
    CreateWithMountedBucket.createScriptJobWithBucket(PROJECT_ID, REGION, SCRIPT_JOB_NAME,
        BUCKET_NAME);
    Job job = Util.getJob(PROJECT_ID, REGION, SCRIPT_JOB_NAME);
    Util.waitForJobCompletion(job);
    assertThat(stdOut.toString()).contains("Successfully created the job");
    testBucketContent();
  }

  // This method is called from testcase: `testBucketJob`
  // This is not a standalone testcase.
  public void testBucketContent() {
    String fileNameTemplate = "output_task_%s.txt";
    String fileContentTemplate;

    Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    Bucket bucket = storage.get(BUCKET_NAME);
    for (int i = 0; i < 4; i++) {
      fileContentTemplate = String.format("Hello world from task %s.\n", i);
      String fileName = String.format(fileNameTemplate, i);
      Blob blob = bucket.get(fileName);
      if (blob == null) {
        throw new MissingResourceException("Cannot find file in bucket.", Blob.class.getName(),
            fileName);
      }
      String content = new String(blob.getContent(), StandardCharsets.UTF_8);
      assertThat(fileContentTemplate).matches(content);
    }
  }
}
