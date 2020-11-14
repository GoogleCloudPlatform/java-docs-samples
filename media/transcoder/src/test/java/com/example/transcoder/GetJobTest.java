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

import com.example.transcoder.CreateJobFromAdHoc;
import com.example.transcoder.DeleteJob;
import com.example.transcoder.GetJob;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageClass;
import com.google.cloud.storage.StorageOptions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class GetJobTest {

  private static final String LOCATION = "us-central1";
  private static final String BUCKET_NAME = "java-samples-transcoder-test";
  private static final String TEST_FILE_NAME = "ChromeCast.mp4";
  private static final String TEST_FILE_PATH =
      "src/test/java/com/example/transcoder/testdata/" + TEST_FILE_NAME;
  private static final String INPUT_URI = "gs://" + BUCKET_NAME + "/" + TEST_FILE_NAME;
  private static final String OUTPUT_URI_FOR_AD_HOC = "gs://" + BUCKET_NAME + "/test-output-adhoc/";
  private static String PROJECT_ID;
  private static String PROJECT_NUMBER;
  private static String JOB_ID;
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

    Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    Bucket bucket = storage.get(BUCKET_NAME);
    if (bucket != null) {
      Page<Blob> blobs = bucket.list();

      for (Blob blob : blobs.iterateAll()) {
        System.out.println(blob.getName());
        storage.delete(BUCKET_NAME, blob.getName());
      }
      bucket.delete();
    }

    bucket =
        storage.create(
            BucketInfo.newBuilder(BUCKET_NAME)
                .setStorageClass(StorageClass.STANDARD)
                .setLocation(LOCATION)
                .build());

    BlobId blobId = BlobId.of(BUCKET_NAME, TEST_FILE_NAME);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
    Path path = Paths.get(TEST_FILE_PATH);
    storage.create(blobInfo, Files.readAllBytes(path));

    String jobName = String.format("projects/%s/locations/%s/jobs/", PROJECT_NUMBER, LOCATION);
    try {
      CreateJobFromAdHoc.createJobFromAdHoc(PROJECT_ID, LOCATION, INPUT_URI, OUTPUT_URI_FOR_AD_HOC);
    } catch (GoogleJsonResponseException gjre) {
    }
    String output = bout.toString();
    assertThat(output, containsString(jobName));
    String[] arr = output.split("/");
    JOB_ID = arr[arr.length - 1].replace("\n", "");
    bout.reset();
  }

  @Test
  public void test_GetJob() throws Exception {
    try {
      GetJob.getJob(PROJECT_ID, LOCATION, JOB_ID);
    } catch (GoogleJsonResponseException gjre) {
    }
    String output = bout.toString();
    String jobName =
        String.format("projects/%s/locations/%s/jobs/%s", PROJECT_NUMBER, LOCATION, JOB_ID);
    assertThat(output, containsString(jobName));
    bout.reset();
  }

  @After
  public void tearDown() throws IOException {
    try {
      DeleteJob.deleteJob(PROJECT_ID, LOCATION, JOB_ID);
    } catch (GoogleJsonResponseException gjre) {
    }
    System.setOut(originalOut);
    bout.reset();
  }
}
