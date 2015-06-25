package com.google.storagetransfer.samples;

import java.io.IOException;
import java.util.logging.Logger;

import com.google.api.services.storagetransfer_googleapis.StoragetransferGoogleapis;
import com.google.api.services.storagetransfer_googleapis.model.ListOperationsResponse;

public class RequestChecker {
  
  // User-provided constants
  private static final String projectId = "";
  private static final String jobId = "";
  private static final Logger log = Logger.getLogger(RequestChecker.class.getName());

  public static void checkTransfer(StoragetransferGoogleapis client, String projectId, String jobId)
    throws InstantiationException, IllegalAccessException, IOException {
    ListOperationsResponse r = client.transferOperations().list("transferOperations")
      .setFilter("{\"project_id\": \"" + projectId + "\", \"job_id\": [\"" + jobId + "\"] }")
      .execute();
    log.info("Result of transferOperations/list: " + r.toPrettyString());
  }
  
  public static void main(String[] args) throws InstantiationException, IllegalAccessException,
    IOException {
    checkTransfer(TransferClientCreator.createStorageTransferClient(), projectId, jobId);
  }
}