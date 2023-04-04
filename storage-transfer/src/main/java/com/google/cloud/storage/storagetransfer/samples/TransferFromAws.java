/*
 * Copyright 2021 Google Inc.
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

// [START storagetransfer_transfer_from_aws]

import com.google.storagetransfer.v1.proto.StorageTransferServiceClient;
import com.google.storagetransfer.v1.proto.TransferProto.CreateTransferJobRequest;
import com.google.storagetransfer.v1.proto.TransferTypes.AwsAccessKey;
import com.google.storagetransfer.v1.proto.TransferTypes.AwsS3Data;
import com.google.storagetransfer.v1.proto.TransferTypes.GcsData;
import com.google.storagetransfer.v1.proto.TransferTypes.Schedule;
import com.google.storagetransfer.v1.proto.TransferTypes.TransferJob;
import com.google.storagetransfer.v1.proto.TransferTypes.TransferJob.Status;
import com.google.storagetransfer.v1.proto.TransferTypes.TransferSpec;
import com.google.type.Date;
import com.google.type.TimeOfDay;
import java.io.IOException;
import java.util.Calendar;

public class TransferFromAws {

  // Creates a one-off transfer job from Amazon S3 to Google Cloud Storage.
  public static void transferFromAws(
      String projectId,
      String jobDescription,
      String awsSourceBucket,
      String gcsSinkBucket,
      long startDateTime)
      throws IOException {

    // Your Google Cloud Project ID
    // String projectId = "your-project-id";

    // A short description of this job
    // String jobDescription = "Sample transfer job from S3 to GCS.";

    // The name of the source AWS bucket to transfer data from
    // String awsSourceBucket = "yourAwsSourceBucket";

    // The name of the GCS bucket to transfer data to
    // String gcsSinkBucket = "your-gcs-bucket";

    // What day and time in UTC to start the transfer, expressed as an epoch date timestamp.
    // If this is in the past relative to when the job is created, it will run the next day.
    // long startDateTime =
    //     new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2000-01-01 00:00:00").getTime();

    // The ID used to access your AWS account. Should be accessed via environment variable.
    String awsAccessKeyId = System.getenv("AWS_ACCESS_KEY_ID");

    // The Secret Key used to access your AWS account. Should be accessed via environment variable.
    String awsSecretAccessKey = System.getenv("AWS_SECRET_ACCESS_KEY");

    // Set up source and sink
    TransferSpec transferSpec =
        TransferSpec.newBuilder()
            .setAwsS3DataSource(
                AwsS3Data.newBuilder()
                    .setBucketName(awsSourceBucket)
                    .setAwsAccessKey(
                        AwsAccessKey.newBuilder()
                            .setAccessKeyId(awsAccessKeyId)
                            .setSecretAccessKey(awsSecretAccessKey)))
            .setGcsDataSink(GcsData.newBuilder().setBucketName(gcsSinkBucket))
            .build();

    // Parse epoch timestamp into the model classes
    Calendar startCalendar = Calendar.getInstance();
    startCalendar.setTimeInMillis(startDateTime);
    // Note that this is a Date from the model class package, not a java.util.Date
    Date startDate =
        Date.newBuilder()
            .setYear(startCalendar.get(Calendar.YEAR))
            .setMonth(startCalendar.get(Calendar.MONTH) + 1)
            .setDay(startCalendar.get(Calendar.DAY_OF_MONTH))
            .build();
    TimeOfDay startTime =
        TimeOfDay.newBuilder()
            .setHours(startCalendar.get(Calendar.HOUR_OF_DAY))
            .setMinutes(startCalendar.get(Calendar.MINUTE))
            .setSeconds(startCalendar.get(Calendar.SECOND))
            .build();
    Schedule schedule =
        Schedule.newBuilder()
            .setScheduleStartDate(startDate)
            .setScheduleEndDate(startDate)
            .setStartTimeOfDay(startTime)
            .build();

    // Set up the transfer job
    TransferJob transferJob =
        TransferJob.newBuilder()
            .setDescription(jobDescription)
            .setProjectId(projectId)
            .setTransferSpec(transferSpec)
            .setSchedule(schedule)
            .setStatus(Status.ENABLED)
            .build();

    // Create a Transfer Service client
    StorageTransferServiceClient storageTransfer = StorageTransferServiceClient.create();

    // Create the transfer job
    TransferJob response =
        storageTransfer.createTransferJob(
            CreateTransferJobRequest.newBuilder().setTransferJob(transferJob).build());

    System.out.println("Created transfer job from AWS to GCS:");
    System.out.println(response.toString());
  }
}
// [END storagetransfer_transfer_from_aws]
