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

package com.google.cloud.storage.storagetransfer.samples.test;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.storage.storagetransfer.samples.TransferFromAws;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TransferFromAwsTest {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");

  @Test
  public void testTransferFromAws() throws Exception {
    PrintStream standardOut = System.out;
    final ByteArrayOutputStream sampleOutputCapture = new ByteArrayOutputStream();
    System.setOut(new PrintStream(sampleOutputCapture));

    TransferFromAws.transferFromAws(PROJECT_ID, "Sample transfer job from S3 to GCS.",
        "cloud-samples-tests", PROJECT_ID + "-storagetransfer",
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2000-01-01 00:00:00").getTime(),
        System.getenv("AWS_ACCESS_KEY_ID"), System.getenv("AWS_SECRET_ACCESS_KEY"));

    String sampleOutput = sampleOutputCapture.toString();
    System.setOut(standardOut);
    assertThat(sampleOutput).contains("\"description\" : \"Sample transfer job from S3 to GCS.\"");
  }
}
