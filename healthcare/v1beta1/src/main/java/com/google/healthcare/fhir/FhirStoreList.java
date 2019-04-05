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

package com.google.healthcare.fhir;

// [START healthcare_list_fhir_stores]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.model.FhirStore;
import com.google.api.services.healthcare.v1beta1.model.ListFhirStoresResponse;
import java.io.IOException;
import java.util.List;

public class FhirStoreList {
  public static void listFhirStores(String projectId, String cloudRegion, String datasetId)
      throws IOException {
    String parentName =
        String.format("projects/%s/locations/%s/datasets/%s", projectId, cloudRegion, datasetId);
    ListFhirStoresResponse response =
        HealthcareQuickstart.getCloudHealthcareClient()
            .projects()
            .locations()
            .datasets()
            .fhirStores()
            .list(parentName)
            .execute();
    List<FhirStore> fhirStores = response.getFhirStores();
    if (fhirStores == null) {
      System.out.println("Retrieved 0 FHIR stores");
      return;
    }
    System.out.println("Retrieved " + fhirStores.size() + " FHIR stores");
    for (int i = 0; i < fhirStores.size(); i++) {
      System.out.println("  - " + fhirStores.get(i).getName());
    }
  }
}
// [END healthcare_list_fhir_stores]
