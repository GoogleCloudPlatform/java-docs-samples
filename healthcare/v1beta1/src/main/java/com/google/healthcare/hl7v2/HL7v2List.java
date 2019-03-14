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

package com.google.healthcare.hl7v2;

// [START healthcare_list_hl7v2_stores]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.model.Hl7V2Store;
import com.google.api.services.healthcare.v1beta1.model.ListHl7V2StoresResponse;

import java.io.IOException;
import java.util.List;

public class HL7v2List {
  public static void listHL7v2Stores(String projectId, String cloudRegion, String datasetId) throws IOException {
    String parentName = String.format(
        "projects/%s/locations/%s/datasets/%s",
        projectId,
        cloudRegion,
        datasetId);
    ListHl7V2StoresResponse response = HealthcareQuickstart.getCloudHealthcareClient()
        .projects()
        .locations()
        .datasets()
        .hl7V2Stores()
        .list(parentName)
        .execute();
    List<Hl7V2Store> hl7V2Stores = response.getHl7V2Stores();
    if (hl7V2Stores == null) {
      System.out.println("Retrieved 0 HL7v2 stores");
      return;
    }
    System.out.println("Retrieved " + hl7V2Stores.size() + " HL7v2 stores");
    for (int i = 0; i < hl7V2Stores.size(); i++) {
      System.out.println("  - " + hl7V2Stores.get(i).getName());
    }
  }
}
// [END healthcare_list_hl7v2_stores]
