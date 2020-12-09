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

import com.google.cloud.storage.storagetransfer.samples.TransferToNearline;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import org.junit.Test;

public class TransferToNearlineTest {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");

  @Test
  public void testTransferToNearline() throws Exception {
    PrintStream standardOut = System.out;
    final ByteArrayOutputStream sampleOutputCapture = new ByteArrayOutputStream();
    System.setOut(new PrintStream(sampleOutputCapture));

    TransferToNearline
        .transferToNearline(PROJECT_ID, "Sample transfer job from GCS to GCS Nearline.",
            PROJECT_ID + "-storagetransfer-source", PROJECT_ID + "-storagetransfer-sink",
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2000-01-01 00:00:00").getTime());

    String sampleOutput = sampleOutputCapture.toString();
    System.setOut(standardOut);
    assertThat(sampleOutput)
        .contains("\"description\" : \"Sample transfer job from GCS to GCS Nearline.\"");
  }
}
