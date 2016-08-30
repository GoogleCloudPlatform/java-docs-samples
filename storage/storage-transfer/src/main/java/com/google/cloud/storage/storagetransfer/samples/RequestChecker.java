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
import com.google.api.services.storagetransfer.model.ListOperationsResponse;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Queries for TransferOperations associated with a specific TransferJob. A TransferJob is done when
 * all of its associated TransferOperations have completed.
 *
 */
public final class RequestChecker {

  private static final String PROJECT_ID = "YOUR_PROJECT_ID";
  private static final String JOB_NAME = "YOUR_JOB_NAME";

  private static final Logger LOG = Logger.getLogger(RequestChecker.class.getName());

  /**
   * Creates and executes a query for all associated TransferOperations.
   *
   * @param client
   *          a Storagetransfer client, for interacting with the Storage Transfer API
   * @param projectId
   *          the project to query within
   * @param jobName
   *          the job Name of the relevant TransferJob
   * @return an object containing information on associated TransferOperations
   * @throws IOException
   *           if the client failed to complete the request
   */
  public static ListOperationsResponse checkTransfer(
      Storagetransfer client, String projectId, String jobName) throws IOException {
    return client
        .transferOperations()
        .list("transferOperations")
        .setFilter("{\"project_id\": \"" + projectId + "\", \"job_names\": [\"" + jobName + "\"] }")
        .execute();
  }

  /**
   * Output the returned list of TransferOperations.
   *
   * @param args
   *          arguments from the command line
   */
  public static void main(String[] args) {
    try {
      ListOperationsResponse resp =
          checkTransfer(TransferClientCreator.createStorageTransferClient(), PROJECT_ID, JOB_NAME);
      LOG.info("Result of transferOperations/list: " + resp.toPrettyString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
//[END all]
