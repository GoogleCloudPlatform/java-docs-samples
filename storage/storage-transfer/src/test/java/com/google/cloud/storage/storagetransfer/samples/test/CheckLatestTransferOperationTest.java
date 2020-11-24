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
import com.google.cloud.storage.storagetransfer.samples.CheckLatestTransferOperation;
import com.google.cloud.storage.storagetransfer.samples.TransferClientCreator;
import com.google.cloud.storage.storagetransfer.samples.TransferJobUtils;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Test;

public class CheckLatestTransferOperationTest {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");

  @Test
  public void testCheckLatestTransferOperation() throws Exception {
    Date date = TransferJobUtils.createDate("2000-01-01");
    TimeOfDay time = TransferJobUtils.createTimeOfDay("00:00:00");
    TransferJob transferJob =
        new TransferJob()
            .setDescription("Sample job")
            .setProjectId(PROJECT_ID)
            .setTransferSpec(
                new TransferSpec()
                    .setGcsDataSource(new GcsData().setBucketName(PROJECT_ID + "-storagetransfer-source"))
                    .setGcsDataSink(new GcsData().setBucketName(PROJECT_ID + "-storagetransfer-sink"))
                    .setObjectConditions(
                        new ObjectConditions()
                            .setMinTimeElapsedSinceLastModification("2592000s" /* 30 days */))
                    .setTransferOptions(
                        new TransferOptions().setDeleteObjectsFromSourceAfterTransfer(true)))
            .setSchedule(new Schedule().setScheduleStartDate(date).setStartTimeOfDay(time))
            .setStatus("ENABLED");

    Storagetransfer client = TransferClientCreator.createStorageTransferClient();
    TransferJob response  = client.transferJobs().create(transferJob).execute();

    PrintStream standardOut = System.out;
    final ByteArrayOutputStream sampleOutputCapture = new ByteArrayOutputStream();
    System.setOut(new PrintStream(sampleOutputCapture));

    CheckLatestTransferOperation.CheckLatestTransferOperation(PROJECT_ID, response.getName());

    String sampleOutput = sampleOutputCapture.toString();
    System.setOut(standardOut);
    assertThat(sampleOutput.contains(response.getName()));
  }
}