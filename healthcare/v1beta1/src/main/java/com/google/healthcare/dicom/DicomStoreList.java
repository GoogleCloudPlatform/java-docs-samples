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

// [START healthcare_list_dicom_stores]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.model.DicomStore;
import com.google.api.services.healthcare.v1beta1.model.ListDicomStoresResponse;

import java.io.IOException;
import java.util.List;

public class DicomStoreList {
  public static void listDicomStores(String projectId, String cloudRegion, String datasetId) throws IOException {
    String parentName = String.format(
        "projects/%s/locations/%s/datasets/%s",
        projectId,
        cloudRegion,
        datasetId);
    ListDicomStoresResponse response = HealthcareQuickstart.getCloudHealthcareClient()
        .projects()
        .locations()
        .datasets()
        .dicomStores()
        .list(parentName)
        .execute();
    List<DicomStore> dicomStores = response.getDicomStores();
    if (dicomStores == null) {
      System.out.println("Retrieved 0 Dicom stores");
      return;
    }
    System.out.println("Retrieved " + dicomStores.size() + " Dicom stores");
    for (int i = 0; i < dicomStores.size(); i++) {
      System.out.println("  - " + dicomStores.get(i).getName());
    }
  }
}
// [END healthcare_list_dicom_stores]
