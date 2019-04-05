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

// [START healthcare_create_dicom_store]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.CloudHealthcare;
import com.google.api.services.healthcare.v1beta1.model.DicomStore;
import com.google.gson.Gson;
import java.io.IOException;

public class DicomStoreCreate {
  private static final Gson GSON = new Gson();

  public static void createDicomStore(
      String projectId, String cloudRegion, String datasetId, String dicomStoreId)
      throws IOException {
    String parentName =
        String.format("projects/%s/locations/%s/datasets/%s", projectId, cloudRegion, datasetId);
    DicomStore dicomStore = new DicomStore();
    CloudHealthcare.Projects.Locations.Datasets.DicomStores.Create createRequest =
        HealthcareQuickstart.getCloudHealthcareClient()
            .projects()
            .locations()
            .datasets()
            .dicomStores()
            .create(parentName, dicomStore);
    createRequest.setDicomStoreId(dicomStoreId);

    dicomStore = createRequest.execute();

    System.out.println("Created Dicom store: " + GSON.toJson(dicomStore));
  }
}
// [END healthcare_create_dicom_store]
