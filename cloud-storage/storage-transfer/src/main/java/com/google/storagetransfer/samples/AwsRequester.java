package com.google.storagetransfer.samples;

import java.io.IOException;
import java.util.logging.Logger;

import com.google.api.services.storagetransfer_googleapis.StoragetransferGoogleapis;
import com.google.api.services.storagetransfer_googleapis.model.AwsAccessKey;
import com.google.api.services.storagetransfer_googleapis.model.AwsS3Data;
import com.google.api.services.storagetransfer_googleapis.model.Date;
import com.google.api.services.storagetransfer_googleapis.model.GcsData;
import com.google.api.services.storagetransfer_googleapis.model.Schedule;
import com.google.api.services.storagetransfer_googleapis.model.TimeOfDay;
import com.google.api.services.storagetransfer_googleapis.model.TransferJob;
import com.google.api.services.storagetransfer_googleapis.model.TransferSpec;

public class AwsRequester {

  // User-provided constants
  private static final String projectId = "";
  private static final String projectDesc = "";
  private static final String awsSourceName = "";
  private static final String awsAccessKeyId = "";
  private static final String awsSecretAccessKey = "";
  private static final String gcsSinkName = "";
  // US Pacific Time Zone
  private static final String startDate = "YYYY-MM-DD";
  private static final String startTime = "HH:MM:SS"; 
  
  private static final Logger log = Logger.getLogger(AwsRequester.class.getName());
  
  public static void createAwsRequest(StoragetransferGoogleapis client, String projectId,
    String desc, Date date, TimeOfDay time, String awsSourceName, String awsAccessKeyId,
    String awsSecretAccessKey, String gcsSinkName)
    throws InstantiationException, IllegalAccessException, IOException {
    TransferJob t = TransferJob.class
      .newInstance()
      .setProjectId(projectId)
      .setSchedule(
        Schedule.class.newInstance()
          .setScheduleStartDate(date)
          .setScheduleEndDate(date)
          .setStartTimeOfDay(time))
      .setTransferSpec(
        TransferSpec.class.newInstance()
          .setAwsS3DataSource(
            AwsS3Data.class.newInstance()
              .setBucketName(awsSourceName)
              .setAwsAccessKey(AwsAccessKey.class.newInstance().setAccessKeyId(awsAccessKeyId)
              .setSecretAccessKey(awsSecretAccessKey)))
          .setGcsDataSink(
            GcsData.class.newInstance().setBucketName(gcsSinkName)))
      .setJobDescription(desc).setJobState("ENABLED");
    
    TransferJob responseT = client.transferJobs().create(t).execute();
    log.info("Return transferJob: " + responseT.toPrettyString());
  }
  
  public static void main(String[] args) throws IOException, NumberFormatException,
    InstantiationException, IllegalAccessException {
    Date date = Date.class.newInstance().setYear(Integer.decode(startDate.split("-")[0]))
      .setMonth(Integer.decode(startDate.split("-")[1]))
      .setDay(Integer.decode(startDate.split("-")[2]));
    TimeOfDay time = TimeOfDay.class.newInstance()
      .setHours(Integer.decode(startTime.split(":")[0]))
      .setMinutes(Integer.decode(startTime.split(":")[1]))
      .setSeconds(Integer.decode(startTime.split(":")[2]));
    createAwsRequest(TransferClientCreator.createStorageTransferClient(), projectId, projectDesc,
      date, time, awsSourceName, awsAccessKeyId, awsSecretAccessKey, gcsSinkName);
  }
}
