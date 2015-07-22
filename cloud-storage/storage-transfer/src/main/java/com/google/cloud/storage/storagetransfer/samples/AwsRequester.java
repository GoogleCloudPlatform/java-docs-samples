/**
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
// [START all]
package com.google.cloud.storage.storagetransfer.samples;

import com.google.api.services.storagetransfer.Storagetransfer;
import com.google.api.services.storagetransfer.model.AwsAccessKey;
import com.google.api.services.storagetransfer.model.AwsS3Data;
import com.google.api.services.storagetransfer.model.Date;
import com.google.api.services.storagetransfer.model.GcsData;
import com.google.api.services.storagetransfer.model.Schedule;
import com.google.api.services.storagetransfer.model.TimeOfDay;
import com.google.api.services.storagetransfer.model.TransferJob;
import com.google.api.services.storagetransfer.model.TransferSpec;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Creates a one-off transfer job from Amazon S3 to Google Cloud Storage.
 */
public final class AwsRequester {

  private static final String JOB_DESC = "YOUR DESCRIPTION";
  private static final String PROJECT_ID = "YOUR_PROJECT_ID";
  private static final String AWS_SOURCE_NAME = "YOUR SOURCE BUCKET";
  private static final String AWS_ACCESS_KEY_ID = "YOUR_ACCESS_KEY_ID";
  private static final String AWS_SECRET_ACCESS_KEY = "YOUR_SECRET_ACCESS_KEY";
  private static final String GCS_SINK_NAME = "YOUR_SINK_BUCKET";

  /**
   * Specify times below using US Pacific Time Zone.
   */
  private static final String START_DATE = "YYYY-MM-DD";
  private static final String START_TIME = "HH:MM:SS";

  private static final Logger LOG = Logger.getLogger(AwsRequester.class.getName());

  /**
   * Creates and executes a request for a TransferJob from Amazon S3 to Cloud Storage.
   *
   * @return the response TransferJob if the request is successful
   * @throws InstantiationException
   *           if instantiation fails when building the TransferJob
   * @throws IllegalAccessException
   *           if an illegal access occurs when building the TransferJob
   * @throws IOException
   *           if the client failed to complete the request
   */
  public static TransferJob createAwsTransferJob() throws InstantiationException,
    IllegalAccessException, IOException {
    Date date = TransferJobUtils.createDate(START_DATE);
    TimeOfDay time = TransferJobUtils.createTimeOfDay(START_TIME);
    TransferJob transferJob = TransferJob.class
        .newInstance()
        .setDescription(JOB_DESC)
        .setProjectId(PROJECT_ID)
        .setTransferSpec(
        TransferSpec.class
          .newInstance()
          .setAwsS3DataSource(
            AwsS3Data.class
              .newInstance()
              .setBucketName(AWS_SOURCE_NAME)
              .setAwsAccessKey(
                AwsAccessKey.class.newInstance().setAccessKeyId(AWS_ACCESS_KEY_ID)
                  .setSecretAccessKey(AWS_SECRET_ACCESS_KEY)))
          .setGcsDataSink(GcsData.class.newInstance().setBucketName(GCS_SINK_NAME)))
        .setSchedule(
        Schedule.class.newInstance().setScheduleStartDate(date).setScheduleEndDate(date)
          .setStartTimeOfDay(time)).setStatus("ENABLED");

    Storagetransfer client = TransferClientCreator.createStorageTransferClient();
    return client.transferJobs().create(transferJob).execute();
  }

  /**
   * Output the contents of a successfully created TransferJob.
   *
   * @param args
   *          arguments from the command line
   */
  public static void main(String[] args) {
    try {
      TransferJob responseT = createAwsTransferJob();
      LOG.info("Return transferJob: " + responseT.toPrettyString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
//[END all]
