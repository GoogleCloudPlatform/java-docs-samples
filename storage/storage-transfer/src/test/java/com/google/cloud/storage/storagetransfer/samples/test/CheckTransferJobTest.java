package com.google.cloud.storage.storagetransfer.samples.test;

import static com.google.common.truth.Truth.assertThat;

import com.google.api.client.googleapis.util.Utils;
import com.google.api.services.storagetransfer.v1.Storagetransfer;
import com.google.api.services.storagetransfer.v1.StoragetransferScopes;
import com.google.api.services.storagetransfer.v1.model.Date;
import com.google.api.services.storagetransfer.v1.model.GcsData;
import com.google.api.services.storagetransfer.v1.model.ObjectConditions;
import com.google.api.services.storagetransfer.v1.model.Schedule;
import com.google.api.services.storagetransfer.v1.model.TimeOfDay;
import com.google.api.services.storagetransfer.v1.model.TransferJob;
import com.google.api.services.storagetransfer.v1.model.TransferOptions;
import com.google.api.services.storagetransfer.v1.model.TransferSpec;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.storagetransfer.samples.CheckTransferJob;
import com.google.cloud.storage.storagetransfer.samples.TransferClientCreator;
import com.google.cloud.storage.storagetransfer.samples.TransferJobUtils;
import com.google.cloud.storage.storagetransfer.samples.TransferToNearline;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    CheckTransferJob.CheckTransferJob(PROJECT_ID, response.getName());

    String sampleOutput = sampleOutputCapture.toString();
    System.setOut(standardOut);
    // An exception would be thrown if the call failed, so if it reaches this line it went through
    assertThat(sampleOutput).contains("List operation returned response:");
  }
}
