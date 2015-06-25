package com.google.storagetransfer.samples;

import java.io.IOException;
import java.util.logging.Logger;

import com.google.api.services.storagetransfer_googleapis.StoragetransferGoogleapis;
import com.google.api.services.storagetransfer_googleapis.model.Date;
import com.google.api.services.storagetransfer_googleapis.model.GcsData;
import com.google.api.services.storagetransfer_googleapis.model.ObjectConditions;
import com.google.api.services.storagetransfer_googleapis.model.Schedule;
import com.google.api.services.storagetransfer_googleapis.model.TimeOfDay;
import com.google.api.services.storagetransfer_googleapis.model.TransferJob;
import com.google.api.services.storagetransfer_googleapis.model.TransferOptions;
import com.google.api.services.storagetransfer_googleapis.model.TransferSpec;

public class NearlineRequester {

  // User-provided constants
  private static final String projectId = "";
  private static final String projectDesc = "";
  private static final String gcsSourceName = "";
  private static final String gcsSinkName = "";
  // US Pacific Time Zone
  private static final String startDate = "YYYY-MM-DD";
  private static final String startTime = "HH:MM:SS";
  
  private static final Logger log = Logger.getLogger(NearlineRequester.class.getName());
  
  public static void createNearlineRequest(StoragetransferGoogleapis client, String projectId,
    String desc, Date date, TimeOfDay time, String gcsSourceName, String gcsNearlineName)
    throws InstantiationException,
    IllegalAccessException, IOException {
    TransferJob t = TransferJob.class
      .newInstance()
      .setProjectId(projectId)
      .setSchedule(
        Schedule.class.newInstance()
          .setScheduleStartDate(date)
          .setStartTimeOfDay(time))
      .setTransferSpec(
        TransferSpec.class.newInstance()
          .setGcsDataSource(
            GcsData.class.newInstance().setBucketName(gcsSourceName))
          .setGcsDataSink(
            GcsData.class.newInstance().setBucketName(gcsNearlineName))
          .setObjectConditions(
            ObjectConditions.class.newInstance().setMinTimeElapsedSinceLastModification("2592000s"))
          .setTransferOptions(
            TransferOptions.class.newInstance().setDeleteObjectsFromSourceAfterTransfer(true)))
      .setJobDescription(desc).setJobState("ENABLED");
    
    TransferJob responseT = client.transferJobs().create(t).execute();
    log.info("Return transferJob: " + responseT.toPrettyString());
  }
  
  public static void main(String[] args) throws NumberFormatException, InstantiationException,
    IllegalAccessException, IOException {
    Date date = Date.class.newInstance().setYear(Integer.decode(startDate.split("-")[0]))
      .setMonth(Integer.decode(startDate.split("-")[1]))
      .setDay(Integer.decode(startDate.split("-")[2]));
    TimeOfDay time = TimeOfDay.class.newInstance()
      .setHours(Integer.decode(startTime.split(":")[0]))
      .setMinutes(Integer.decode(startTime.split(":")[1]))
      .setSeconds(Integer.decode(startTime.split(":")[2]));
    createNearlineRequest(TransferClientCreator.createStorageTransferClient(), projectId,
      projectDesc, date, time, gcsSourceName, gcsSinkName);
  }
}
