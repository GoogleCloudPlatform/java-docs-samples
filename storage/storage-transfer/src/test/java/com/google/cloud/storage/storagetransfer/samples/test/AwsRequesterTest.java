/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

@RunWith(JUnit4.class)
public class AwsRequesterTest {

  /**
   * Tests whether AwsRequester executes a request to create a TransferJob.
   */
  @Test
  public void testRun() throws Exception {
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    PrintStream outStream = new PrintStream(outBytes);
    System.setProperty("projectId", "cloud-samples-tests");
    System.setProperty("jobDescription", "Sample transfer job from S3 to GCS.");
    System.setProperty("awsSourceBucket", "cloud-samples-tests");
    System.setProperty("gcsSinkBucket", "cloud-samples-tests-storagetransfer");
    System.setProperty("startDate", "2000-01-01");
    System.setProperty("startTime", "00:00:00");

    AwsRequester.run(outStream);
    String out = outBytes.toString();

    assertThat(out).contains("\"description\" : \"Sample transfer job from S3 to GCS.\"");
  }
}