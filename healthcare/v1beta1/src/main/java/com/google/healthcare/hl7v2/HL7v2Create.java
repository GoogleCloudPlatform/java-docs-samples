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

// [START healthcare_create_hl7v2_store]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.CloudHealthcare;
import com.google.api.services.healthcare.v1beta1.model.Hl7V2Store;
import com.google.gson.Gson;
import java.io.IOException;

public class HL7v2Create {
  private static final Gson GSON = new Gson();

  public static void createHL7v2Store(
      String projectId, String cloudRegion, String datasetId, String hl7v2StoreId)
      throws IOException {
    String parentName =
        String.format("projects/%s/locations/%s/datasets/%s", projectId, cloudRegion, datasetId);
    Hl7V2Store hl7v2Store = new Hl7V2Store();
    CloudHealthcare.Projects.Locations.Datasets.Hl7V2Stores.Create createRequest =
        HealthcareQuickstart.getCloudHealthcareClient()
            .projects()
            .locations()
            .datasets()
            .hl7V2Stores()
            .create(parentName, hl7v2Store);
    createRequest.setHl7V2StoreId(hl7v2StoreId);

    hl7v2Store = createRequest.execute();

    System.out.println("Created HL7v2 store: " + GSON.toJson(hl7v2Store));
  }
}
// [END healthcare_create_hl7v2_store]
