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

// [START all]

package com.google.cloud.storage.storagetransfer.samples;

import com.google.api.services.storagetransfer.v1.Storagetransfer;
import com.google.api.services.storagetransfer.v1.model.AwsAccessKey;
import com.google.api.services.storagetransfer.v1.model.AwsS3Data;
import com.google.api.services.storagetransfer.v1.model.Date;
import com.google.api.services.storagetransfer.v1.model.GcsData;
import com.google.api.services.storagetransfer.v1.model.Schedule;
import com.google.api.services.storagetransfer.v1.model.TimeOfDay;
import com.google.api.services.storagetransfer.v1.model.TransferJob;
import com.google.api.services.storagetransfer.v1.model.TransferSpec;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Creates a one-off transfer job from Amazon S3 to Google Cloud Storage.
 */
public final class AwsRequester {
  /**
   * Creates and executes a request for a TransferJob from Amazon S3 to Cloud Storage.
   *
   * <p>The {@code startDate} and {@code startTime} parameters should be set according to the UTC
   * Time Zone. See:
   * https://developers.google.com/resources/api-libraries/documentation/storagetransfer/v1/java/latest/com/google/api/services/storagetransfer/v1/model/Schedule.html#getStartTimeOfDay()
   *
   * @return the response TransferJob if the request is successful
   * @throws InstantiationException
   *           if instantiation fails when building the TransferJob
   * @throws IllegalAccessException
   *           if an illegal access occurs when building the TransferJob
   * @throws IOException
   *           if the client failed to complete the request
   */
  public static TransferJob createAwsTransferJob(
      String projectId,
      String jobDescription,
      String awsSourceBucket,
      String gcsSinkBucket,
      String startDate,
      String startTime,
      String awsAccessKeyId,
      String awsSecretAccessKey)
      throws InstantiationException, IllegalAccessException, IOException {
    Date date = TransferJobUtils.createDate(startDate);
    TimeOfDay time = TransferJobUtils.createTimeOfDay(startTime);
    TransferJob transferJob =
        new TransferJob()
            .setDescription(jobDescription)
            .setProjectId(projectId)
            .setTransferSpec(
                new TransferSpec()
                    .setAwsS3DataSource(
                        new AwsS3Data()
                            .setBucketName(awsSourceBucket)
                            .setAwsAccessKey(
                                new AwsAccessKey()
                                    .setAccessKeyId(awsAccessKeyId)
                                    .setSecretAccessKey(awsSecretAccessKey)))
                    .setGcsDataSink(new GcsData().setBucketName(gcsSinkBucket)))
            .setSchedule(
                new Schedule()
                    .setScheduleStartDate(date)
                    .setScheduleEndDate(date)
                    .setStartTimeOfDay(time))
            .setStatus("ENABLED");

    Storagetransfer client = TransferClientCreator.createStorageTransferClient();
    return client.transferJobs().create(transferJob).execute();
  }

  public static void run(PrintStream out)
      throws InstantiationException, IllegalAccessException, IOException {
    String projectId = TransferJobUtils.getPropertyOrFail("projectId");
    String jobDescription = TransferJobUtils.getPropertyOrFail("jobDescription");
    String awsSourceBucket = TransferJobUtils.getPropertyOrFail("awsSourceBucket");
    String gcsSinkBucket = TransferJobUtils.getPropertyOrFail("gcsSinkBucket");
    String startDate = TransferJobUtils.getPropertyOrFail("startDate");
    String startTime = TransferJobUtils.getPropertyOrFail("startTime");
    String awsAccessKeyId = TransferJobUtils.getEnvOrFail("AWS_ACCESS_KEY_ID");
    String awsSecretAccessKey = TransferJobUtils.getEnvOrFail("AWS_SECRET_ACCESS_KEY");

    TransferJob responseT =
        createAwsTransferJob(
            projectId,
            jobDescription,
            awsSourceBucket,
            gcsSinkBucket,
            startDate,
            startTime,
            awsAccessKeyId,
            awsSecretAccessKey);
    out.println("Return transferJob: " + responseT.toPrettyString());
  }

  /**
   * Output the contents of a successfully created TransferJob.
   */
  public static void main(String[] args) {
    try {
      run(System.out);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
//[END all]
