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

// [START healthcare_import_dicom_instance]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.model.GoogleCloudHealthcareV1beta1FhirRestGcsSource;
import com.google.api.services.healthcare.v1beta1.model.ImportResourcesRequest;
import com.google.api.services.healthcare.v1beta1.model.Operation;

import java.io.IOException;

public class FhirStoreImport {
  public static void importFhirStore(String fhirStoreName, String uri)
      throws IOException {
    GoogleCloudHealthcareV1beta1FhirRestGcsSource gcsSource =
        new GoogleCloudHealthcareV1beta1FhirRestGcsSource();
    gcsSource.setUri("gs://" + uri);
    ImportResourcesRequest importRequest = new ImportResourcesRequest();
    importRequest.setGcsSource(gcsSource);
    Operation importOperation = HealthcareQuickstart.getCloudHealthcareClient()
        .projects()
        .locations()
        .datasets()
        .fhirStores()
        .healthcareImport(fhirStoreName, importRequest)
        .execute();
    System.out.println("Importing FHIR store operation name: " + importOperation.getName());
  }
}
// [END healthcare_import_dicom_instance]
