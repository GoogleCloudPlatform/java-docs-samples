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

import com.google.api.services.storagetransfer.v1.Storagetransfer;
import com.google.api.services.storagetransfer.v1.model.Date;
import com.google.api.services.storagetransfer.v1.model.GcsData;
import com.google.api.services.storagetransfer.v1.model.ObjectConditions;
import com.google.api.services.storagetransfer.v1.model.Schedule;
import com.google.api.services.storagetransfer.v1.model.TimeOfDay;
import com.google.api.services.storagetransfer.v1.model.TransferJob;
import com.google.api.services.storagetransfer.v1.model.TransferOptions;
import com.google.api.services.storagetransfer.v1.model.TransferSpec;
import com.google.cloud.storage.storagetransfer.samples.CheckTransferJob;
import com.google.cloud.storage.storagetransfer.samples.test.util.TransferClientCreator;
import com.google.cloud.storage.storagetransfer.samples.test.util.TransferJobUtils;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Test;

public class CheckTransferJobTest {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");

  @Test
  public void testCheckTransferJob() throws Exception {
    Date date = TransferJobUtils.createDate("2000-01-01");
    TimeOfDay time = TransferJobUtils.createTimeOfDay("00:00:00");
    TransferJob transferJob =
        new TransferJob()
            .setDescription("Sample job")
            .setProjectId(PROJECT_ID)
            .setTransferSpec(
                new TransferSpec()
                    .setGcsDataSource(
                        new GcsData().setBucketName(PROJECT_ID + "-storagetransfer-source"))
                    .setGcsDataSink(
                        new GcsData().setBucketName(PROJECT_ID + "-storagetransfer-sink"))
                    .setObjectConditions(
                        new ObjectConditions()
                            .setMinTimeElapsedSinceLastModification("2592000s" /* 30 days */))
                    .setTransferOptions(
                        new TransferOptions().setDeleteObjectsFromSourceAfterTransfer(true)))
            .setSchedule(new Schedule().setScheduleStartDate(date).setStartTimeOfDay(time))
            .setStatus("ENABLED");

    Storagetransfer client = TransferClientCreator.createStorageTransferClient();
    TransferJob response = client.transferJobs().create(transferJob).execute();

    PrintStream standardOut = System.out;
    final ByteArrayOutputStream sampleOutputCapture = new ByteArrayOutputStream();
    System.setOut(new PrintStream(sampleOutputCapture));

    CheckTransferJob.checkTransferJob(PROJECT_ID, response.getName());

    String sampleOutput = sampleOutputCapture.toString();
    System.setOut(standardOut);
    // An exception would be thrown if the call failed, so if it reaches this line it went through
    assertThat(sampleOutput).contains("List operation returned response:");
  }
}
