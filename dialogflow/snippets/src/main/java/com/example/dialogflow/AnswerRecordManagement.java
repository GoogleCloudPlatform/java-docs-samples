/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.dialogflow;

// [START dialogflow_update_answer_record]

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.dialogflow.v2.AnswerFeedback;
import com.google.cloud.dialogflow.v2.AnswerRecord;
import com.google.cloud.dialogflow.v2.AnswerRecordName;
import com.google.cloud.dialogflow.v2.AnswerRecordsClient;
import com.google.cloud.dialogflow.v2.UpdateAnswerRecordRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;

public class AnswerRecordManagement {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "my-location";

    // Set id of the answer record to be updated.
    // Answer records are created when a suggestion for the human agent assistant is generated.
    // See details about how to generate an answer record by getting article suggestion here:
    // https://cloud.google.com/agent-assist/docs/article-suggestion.
    String answerRecordId = "my-answer-record-id";

    // Set the value to be updated for answer_feedback.clicked field.
    boolean isClicked = true;
    updateAnswerRecord(projectId, location, answerRecordId, isClicked);
  }

  // Update whether the answer record was clicked.
  public static void updateAnswerRecord(
      String projectId, String location, String answerRecordId, boolean clicked)
      throws ApiException, IOException {
    // Initialize a client for managing AnswerRecords. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (AnswerRecordsClient answerRecordsClient = AnswerRecordsClient.create()) {
      AnswerRecordName answerRecordName =
          AnswerRecordName.ofProjectLocationAnswerRecordName(projectId, location, answerRecordId);
      AnswerFeedback answerFeedback = AnswerFeedback.newBuilder().setClicked(clicked).build();
      AnswerRecord answerRecord =
          AnswerRecord.newBuilder()
              .setName(answerRecordName.toString())
              .setAnswerFeedback(answerFeedback)
              .build();
      // Add a mask to control which field gets updated.
      FieldMask fieldMask = FieldMask.newBuilder().addPaths("answer_feedback").build();

      UpdateAnswerRecordRequest request =
          UpdateAnswerRecordRequest.newBuilder()
              .setAnswerRecord(answerRecord)
              .setUpdateMask(fieldMask)
              .build();
      AnswerRecord response = answerRecordsClient.updateAnswerRecord(request);
      System.out.println("====================");
      System.out.format("AnswerRecord updated:\n");
      System.out.format("Name: %s\n", response.getName());
      System.out.format("Clicked: %s\n", response.getAnswerFeedback().getClicked());
    }
  }
}
// [END dialogflow_update_answer_record]