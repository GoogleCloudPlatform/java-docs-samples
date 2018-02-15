/*
 * Copyright 2015 Google Inc.
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

package com.google.cloud.storage.storagetransfer.samples.test;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.storage.storagetransfer.samples.AwsRequester;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AwsRequesterTest {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");

  /**
   * Tests whether AwsRequester executes a request to create a TransferJob.
   */
  @Test
  public void testRun() throws Exception {
    System.setProperty("projectId", PROJECT_ID);
    System.setProperty("jobDescription", "Sample transfer job from S3 to GCS.");
    System.setProperty("awsSourceBucket", "cloud-samples-tests");
    System.setProperty("gcsSinkBucket", PROJECT_ID + "-storagetransfer");
    System.setProperty("startDate", "2000-01-01");
    System.setProperty("startTime", "00:00:00");

    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    PrintStream outStream = new PrintStream(outBytes);
    AwsRequester.run(outStream);
    String out = outBytes.toString();

    assertThat(out).contains("\"description\" : \"Sample transfer job from S3 to GCS.\"");
  }
}
