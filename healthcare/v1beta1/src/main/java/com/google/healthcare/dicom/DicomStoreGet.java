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

// [START healthcare_get_dicom_store]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.model.DicomStore;
import com.google.gson.Gson;

import java.io.IOException;

public class DicomStoreGet {
  private static final Gson GSON = new Gson();

  public static void getDicomStore(String dicomStoreName) throws IOException {
    DicomStore dicomStore = HealthcareQuickstart.getCloudHealthcareClient()
        .projects()
        .locations()
        .datasets()
        .dicomStores()
        .get(dicomStoreName)
        .execute();
    System.out.println("Retrieved Dicom store: " + GSON.toJson(dicomStore));
  }
}
// [END healthcare_get_dicom_store]
