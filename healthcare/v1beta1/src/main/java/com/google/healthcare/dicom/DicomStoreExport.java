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

// [START healthcare_export_dicom_instance]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.model.ExportDicomDataRequest;
import com.google.api.services.healthcare.v1beta1.model.GoogleCloudHealthcareV1beta1DicomGcsDestination;
import com.google.api.services.healthcare.v1beta1.model.Operation;

import java.io.IOException;

public class DicomStoreExport {
  public static void exportDicomStoreInstance(String dicomStoreName, String uriPrefix)
      throws IOException {
    ExportDicomDataRequest exportRequest = new ExportDicomDataRequest();
    GoogleCloudHealthcareV1beta1DicomGcsDestination gcdDestination =
        new GoogleCloudHealthcareV1beta1DicomGcsDestination();
    gcdDestination.setUriPrefix("gs://" + uriPrefix);
    exportRequest.setGcsDestination(gcdDestination);
    Operation exportOperation = HealthcareQuickstart.getCloudHealthcareClient()
        .projects()
        .locations()
        .datasets()
        .dicomStores()
        .export(dicomStoreName, exportRequest)
        .execute();
    System.out.println("Exporting Dicom store op name: " + exportOperation.getName());
  }
}
// [END healthcare_export_dicom_instance]
