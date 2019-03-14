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

// [START healthcare_export_fhir_resources]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.model.ExportResourcesRequest;
import com.google.api.services.healthcare.v1beta1.model.GoogleCloudHealthcareV1beta1FhirRestGcsDestination;
import com.google.api.services.healthcare.v1beta1.model.Operation;

import java.io.IOException;

public class FhirStoreExport {
  public static void exportFhirStore(String fhirResourceName, String uriPrefix)
      throws IOException {
    ExportResourcesRequest exportRequest = new ExportResourcesRequest();
    GoogleCloudHealthcareV1beta1FhirRestGcsDestination gcdDestination =
        new GoogleCloudHealthcareV1beta1FhirRestGcsDestination();
    gcdDestination.setUriPrefix("gs://" + uriPrefix);
    exportRequest.setGcsDestination(gcdDestination);
    Operation exportOperation = HealthcareQuickstart.getCloudHealthcareClient()
        .projects()
        .locations()
        .datasets()
        .fhirStores()
        .export(fhirResourceName, exportRequest)
        .execute();
    System.out.println("Exporting FHIR store operation name: " + exportOperation.getName());
  }
}
// [END healthcare_export_fhir_resources]
