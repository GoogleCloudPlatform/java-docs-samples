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

// [START healthcare_ingest_hl7v2_message]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.model.IngestMessageRequest;
import com.google.api.services.healthcare.v1beta1.model.IngestMessageResponse;
import com.google.api.services.healthcare.v1beta1.model.Message;
import com.google.gson.Gson;
import java.io.IOException;

public class HL7v2MessageIngest {
  private static final Gson GSON = new Gson();

  public static void ingestHl7v2Message(
      String projectId,
      String cloudRegion,
      String datasetId,
      String hl7v2StoreId,
      String hl7v2MessageName)
      throws IOException {
    Message message =
        HealthcareQuickstart.getCloudHealthcareClient()
            .projects()
            .locations()
            .datasets()
            .hl7V2Stores()
            .messages()
            .get(hl7v2MessageName)
            .execute();
    IngestMessageRequest ingestRequest = new IngestMessageRequest();
    ingestRequest.setMessage(message);
    String parentName =
        String.format(
            "projects/%s/locations/%s/datasets/%s/hl7V2Stores/%s",
            projectId, cloudRegion, datasetId, hl7v2StoreId);
    IngestMessageResponse response =
        HealthcareQuickstart.getCloudHealthcareClient()
            .projects()
            .locations()
            .datasets()
            .hl7V2Stores()
            .messages()
            .ingest(parentName, ingestRequest)
            .execute();
    System.out.println("Ingested HL7v2 message: " + GSON.toJson(response));
  }
}
// [END healthcare_ingest_hl7v2_message]
