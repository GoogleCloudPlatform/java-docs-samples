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

// [START healthcare_create_fhir_store]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.CloudHealthcare;
import com.google.api.services.healthcare.v1beta1.model.FhirStore;

import java.io.IOException;
import java.util.Optional;

public class FhirStoreCreate {
  public static void createFhirStore(String projectId, String cloudRegion, String datasetId, String fhirStoreId)
      throws IOException {
    FhirStore fhirStore = new FhirStore();
    cloudRegion = Optional.of(cloudRegion).orElse("us-central1");

    String parentName = String.format("projects/%s/locations/%s/datasets/%s", projectId, cloudRegion, datasetId);
    CloudHealthcare.Projects.Locations.Datasets.FhirStores.Create createRequest = HealthcareQuickstart.getCloudHealthcareClient()
        .projects()
        .locations()
        .datasets()
        .fhirStores()
        .create(parentName, fhirStore);

    createRequest.setFhirStoreId(fhirStoreId);
    createRequest.execute();

    System.out.println("Created FHIR store: " + createRequest.getFhirStoreId());
  }
}
// [END healthcare_create_fhir_store]
