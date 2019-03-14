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

package com.google.healthcare.dicom;

// [START healthcare_import_dicom_instance]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.model.GoogleCloudHealthcareV1beta1DicomGcsSource;
import com.google.api.services.healthcare.v1beta1.model.ImportDicomDataRequest;
import com.google.api.services.healthcare.v1beta1.model.Operation;

import java.io.IOException;

public class DicomStoreImport {
  public static void importDicomStoreInstance(String dicomStoreName, String uri)
      throws IOException {
    GoogleCloudHealthcareV1beta1DicomGcsSource gcsSource =
        new GoogleCloudHealthcareV1beta1DicomGcsSource();
    gcsSource.setUri("gs://" + uri);
    ImportDicomDataRequest importRequest = new ImportDicomDataRequest();
    importRequest.setGcsSource(gcsSource);
    Operation importOperation = HealthcareQuickstart.getCloudHealthcareClient()
        .projects()
        .locations()
        .datasets()
        .dicomStores()
        .healthcareImport(dicomStoreName, importRequest)
        .execute();
    System.out.println("Importing Dicom store op name: " + importOperation.getName());
  }
}
// [END healthcare_import_dicom_instance]
