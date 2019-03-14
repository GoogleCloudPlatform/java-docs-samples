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

package com.google.healthcare.fhir.resources.history;

// [START healthcare_list_resource_history]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.CloudHealthcare.Projects.Locations.Datasets.FhirStores.Fhir.History.List;
import com.google.api.services.healthcare.v1beta1.model.HttpBody;

import java.io.IOException;

public class FhirResourceListHistory {
  public static void listFhirResourceHistory(String resourceName) throws IOException {
    List request = HealthcareQuickstart.getCloudHealthcareClient()
        .projects()
        .locations()
        .datasets()
        .fhirStores()
        .fhir()
        .history()
        .list(resourceName);
    request.setAccessToken(HealthcareQuickstart.getAccessToken());
    HttpBody httpBody = request.execute();
    System.out.println("Listed FHIR resource history: " + httpBody.getData());
  }
}
// [END healthcare_list_resource_history]
