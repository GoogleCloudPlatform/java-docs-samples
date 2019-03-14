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

// [START healthcare_patch_hl7v2_message]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.model.Message;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Map;

public class HL7v2MessagePatch {
  private static final Gson GSON = new Gson();

  public static void patchHL7v2Message(String hl7v2MessageName, String labelKey, String labelValue) throws IOException {
    Message patched = HealthcareQuickstart.getCloudHealthcareClient()
        .projects()
        .locations()
        .datasets()
        .hl7V2Stores()
        .messages()
        .get(hl7v2MessageName)
        .execute();
    Map<String, String> labels = patched.getLabels();
    if (labels == null) {
      labels = Maps.newHashMap();
    }
    labels.put(labelKey, labelValue);
    patched.setLabels(labels);
    Message response = HealthcareQuickstart.getCloudHealthcareClient()
        .projects()
        .locations()
        .datasets()
        .hl7V2Stores()
        .messages()
        .patch(hl7v2MessageName, patched)
        .setUpdateMask("labels")
        .execute();
    System.out.println("Patched HL7v2 message: " + GSON.toJson(response));
  }
}
// [END healthcare_patch_hl7v2_message]
