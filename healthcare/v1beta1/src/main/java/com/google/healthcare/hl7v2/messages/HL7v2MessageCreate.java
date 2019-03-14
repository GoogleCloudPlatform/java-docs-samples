/*
 * Copyright 2019 Google LLC
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

package com.google.healthcare.hl7v2.messages;

// [START healthcare_create_hl7v2_message]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.model.CreateMessageRequest;
import com.google.api.services.healthcare.v1beta1.model.Message;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class HL7v2MessageCreate {
  private static final Gson GSON = new Gson();

  public static void createHL7v2Message(
      String projectId,
      String cloudRegion,
      String datasetId,
      String hl7v2StoreId,
      String messageFileName) throws IOException {
    String parentName = String.format(
        "projects/%s/locations/%s/datasets/%s/hl7V2Stores/%s",
        projectId,
        cloudRegion,
        datasetId,
        hl7v2StoreId);
    CreateMessageRequest createRequest = new CreateMessageRequest();
    Message message = new Message();
    List<String> lines = Files.readAllLines(Paths.get(messageFileName), Charset.defaultCharset());
    String data = String.join("\n", lines);
    message.setData(data);
    createRequest.setMessage(message);
    Message response = HealthcareQuickstart.getCloudHealthcareClient()
        .projects()
        .locations()
        .datasets()
        .hl7V2Stores()
        .messages()
        .create(parentName, createRequest)
        .execute();
    System.out.println("Created HL7v2 message: " + GSON.toJson(response));
  }
}
// [END healthcare_create_hl7v2_message]
