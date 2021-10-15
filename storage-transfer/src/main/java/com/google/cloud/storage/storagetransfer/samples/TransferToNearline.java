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

package com.google.cloud.storage.storagetransfer.samples;

// [START storagetransfer_transfer_to_nearline]
import com.google.protobuf.Duration;
import com.google.storagetransfer.v1.proto.StorageTransferServiceClient;
import com.google.storagetransfer.v1.proto.TransferProto.CreateTransferJobRequest;
import com.google.storagetransfer.v1.proto.TransferTypes.GcsData;
import com.google.storagetransfer.v1.proto.TransferTypes.ObjectConditions;
import com.google.storagetransfer.v1.proto.TransferTypes.Schedule;
import com.google.storagetransfer.v1.proto.TransferTypes.TransferJob;
import com.google.storagetransfer.v1.proto.TransferTypes.TransferJob.Status;
import com.google.storagetransfer.v1.proto.TransferTypes.TransferOptions;
import com.google.storagetransfer.v1.proto.TransferTypes.TransferSpec;
import com.google.type.Date;
import com.google.type.TimeOfDay;
import java.io.IOException;
import java.util.Calendar;

public class TransferToNearline {
  /**
   * Creates a one-off transfer job that transfers objects in a standard GCS bucket that are more
   * than 30 days old to a Nearline GCS bucket.
   */
  public static void transferToNearline(
      String projectId,
      String jobDescription,
      String gcsSourceBucket,
      String gcsNearlineSinkBucket,
      long startDateTime)
      throws IOException {

    // Your Google Cloud Project ID
    // String projectId = "your-project-id";

    // A short description of this job
    // String jobDescription = "Sample transfer job of old objects to a Nearline GCS bucket.";

    // The name of the source GCS bucket to transfer data from
    // String gcsSourceBucket = "your-gcs-source-bucket";

    // The name of the Nearline GCS bucket to transfer old objects to
    // String gcsSinkBucket = "your-nearline-gcs-bucket";

    // What day and time in UTC to start the transfer, expressed as an epoch date timestamp.
    // If this is in the past relative to when the job is created, it will run the next day.
    // long startDateTime =
    //     new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2000-01-01 00:00:00").getTime();

    // Parse epoch timestamp into the model classes
    Calendar startCalendar = Calendar.getInstance();
    startCalendar.setTimeInMillis(startDateTime);
    // Note that this is a Date from the model class package, not a java.util.Date
    Date date =
        Date.newBuilder()
            .setYear(startCalendar.get(Calendar.YEAR))
            .setMonth(startCalendar.get(Calendar.MONTH) + 1)
            .setDay(startCalendar.get(Calendar.DAY_OF_MONTH))
            .build();
    TimeOfDay time =
        TimeOfDay.newBuilder()
            .setHours(startCalendar.get(Calendar.HOUR_OF_DAY))
            .setMinutes(startCalendar.get(Calendar.MINUTE))
            .setSeconds(startCalendar.get(Calendar.SECOND))
            .build();

    TransferJob transferJob =
        TransferJob.newBuilder()
            .setDescription(jobDescription)
            .setProjectId(projectId)
            .setTransferSpec(
                TransferSpec.newBuilder()
                    .setGcsDataSource(GcsData.newBuilder().setBucketName(gcsSourceBucket))
                    .setGcsDataSink(GcsData.newBuilder().setBucketName(gcsNearlineSinkBucket))
                    .setObjectConditions(
                        ObjectConditions.newBuilder()
                            .setMinTimeElapsedSinceLastModification(
                                Duration.newBuilder().setSeconds(2592000 /* 30 days */)))
                    .setTransferOptions(
                        TransferOptions.newBuilder().setDeleteObjectsFromSourceAfterTransfer(true)))
            .setSchedule(Schedule.newBuilder().setScheduleStartDate(date).setStartTimeOfDay(time))
            .setStatus(Status.ENABLED)
            .build();

    // Create a Transfer Service client
    StorageTransferServiceClient storageTransfer = StorageTransferServiceClient.create();

    // Create the transfer job
    TransferJob response =
        storageTransfer.createTransferJob(
            CreateTransferJobRequest.newBuilder().setTransferJob(transferJob).build());

    System.out.println("Created transfer job from standard bucket to Nearline bucket:");
    System.out.println(response.toString());
  }
}
// [END storagetransfer_transfer_to_nearline]
