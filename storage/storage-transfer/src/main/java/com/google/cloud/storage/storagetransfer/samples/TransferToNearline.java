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
import java.io.IOException;
import java.util.Calendar;

public class TransferToNearline {
  /** Creates a one-off transfer job that transfers objects in a standard GCS bucket that are more
   *  than 30 days old to a Nearline GCS bucket.
   */
  public static void transferToNearline(String projectId,
      String jobDescription,
      String gcsSourceBucket,
      String gcsNearlineSinkBucket,
      long startDateTime) throws IOException {

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
    Date date = new Date()
        .setYear(startCalendar.get(Calendar.YEAR))
        .setMonth(startCalendar.get(Calendar.MONTH) + 1)
        .setDay(startCalendar.get(Calendar.DAY_OF_MONTH));
    TimeOfDay time = new TimeOfDay()
        .setHours(startCalendar.get(Calendar.HOUR_OF_DAY))
        .setMinutes(startCalendar.get(Calendar.MINUTE))
        .setSeconds(startCalendar.get(Calendar.SECOND));

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
                        new TransferOptions().setDeleteObjectsFromSourceAfterTransfer(true)))
            .setSchedule(new Schedule().setScheduleStartDate(date).setStartTimeOfDay(time))
            .setStatus("ENABLED");

    // Create a Transfer Service client
    GoogleCredentials credential = GoogleCredentials.getApplicationDefault();
    if (credential.createScopedRequired()) {
      credential = credential.createScoped(StoragetransferScopes.all());
    }
    Storagetransfer storageTransfer = new Storagetransfer.Builder(Utils.getDefaultTransport(),
        Utils.getDefaultJsonFactory(), new HttpCredentialsAdapter(credential))
        .build();

    // Create the transfer job
    TransferJob response = storageTransfer.transferJobs().create(transferJob).execute();

    System.out.println("Created transfer job from standard bucket to Nearline bucket:");
    System.out.println(response.toPrettyString());
  }
}
// [END storagetransfer_transfer_to_nearline]
