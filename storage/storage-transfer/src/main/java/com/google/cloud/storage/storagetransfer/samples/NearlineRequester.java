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
import com.google.api.services.storagetransfer.v1.model.Date;
import com.google.api.services.storagetransfer.v1.model.GcsData;
import com.google.api.services.storagetransfer.v1.model.ObjectConditions;
import com.google.api.services.storagetransfer.v1.model.Schedule;
import com.google.api.services.storagetransfer.v1.model.TimeOfDay;
import com.google.api.services.storagetransfer.v1.model.TransferJob;
import com.google.api.services.storagetransfer.v1.model.TransferOptions;
import com.google.api.services.storagetransfer.v1.model.TransferSpec;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Creates a daily transfer from a standard Cloud Storage bucket to a Cloud Storage Nearline
 * bucket for files untouched for 30 days.
 */
public final class NearlineRequester {

  /**
   * Creates and executes a request for a TransferJob to Cloud Storage Nearline.
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
  public static TransferJob createNearlineTransferJob(
      String projectId,
      String jobDescription,
      String gcsSourceBucket,
      String gcsNearlineSinkBucket,
      String startDate,
      String startTime)
      throws InstantiationException, IllegalAccessException, IOException {
    Date date = TransferJobUtils.createDate(startDate);
    TimeOfDay time = TransferJobUtils.createTimeOfDay(startTime);
    TransferJob transferJob =
        new TransferJob()
            .setDescription(jobDescription)
            .setProjectId(projectId)
            .setTransferSpec(
                new TransferSpec()
                    .setGcsDataSource(new GcsData().setBucketName(gcsSourceBucket))
                    .setGcsDataSink(new GcsData().setBucketName(gcsNearlineSinkBucket))
                    .setObjectConditions(
                        new ObjectConditions()
                            .setMinTimeElapsedSinceLastModification("2592000s" /* 30 days */))
                    .setTransferOptions(
                        new TransferOptions()
                            .setDeleteObjectsFromSourceAfterTransfer(true)))
            .setSchedule(
                new Schedule().setScheduleStartDate(date).setStartTimeOfDay(time))
            .setStatus("ENABLED");

    Storagetransfer client = TransferClientCreator.createStorageTransferClient();
    return client.transferJobs().create(transferJob).execute();
  }

  public static void run(PrintStream out)
      throws InstantiationException, IllegalAccessException, IOException {
    String projectId = TransferJobUtils.getPropertyOrFail("projectId");
    String jobDescription = TransferJobUtils.getPropertyOrFail("jobDescription");
    String gcsSourceBucket = TransferJobUtils.getPropertyOrFail("gcsSourceBucket");
    String gcsNearlineSinkBucket = TransferJobUtils.getPropertyOrFail("gcsNearlineSinkBucket");
    String startDate = TransferJobUtils.getPropertyOrFail("startDate");
    String startTime = TransferJobUtils.getPropertyOrFail("startTime");

    TransferJob responseT =
        createNearlineTransferJob(
            projectId,
            jobDescription,
            gcsSourceBucket,
            gcsNearlineSinkBucket,
            startDate,
            startTime);
    out.println("Return transferJob: " + responseT.toPrettyString());
  }

  /**
   * Output the contents of a successfully created TransferJob.
   *
   * @param args
   *          arguments from the command line
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
