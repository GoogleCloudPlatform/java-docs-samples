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

package com.google.healthcare.fhir.resources;

// [START healthcare_get_resource]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.CloudHealthcare.Projects.Locations.Datasets.FhirStores.Fhir.Get;
import com.google.api.services.healthcare.v1beta1.model.HttpBody;

import java.io.IOException;

public class FhirResourceGet {
  public static void getFhirResource(String resourceName) throws IOException {
    Get request = HealthcareQuickstart.getCloudHealthcareClient()
        .projects()
        .locations()
        .datasets()
        .fhirStores()
        .fhir()
        .get(resourceName);
    request.setAccessToken(HealthcareQuickstart.getAccessToken());
    HttpBody httpBody = request.execute();
    System.out.println("Retrieved FHIR resource: " + httpBody.getData());
  }
}
// [END healthcare_get_resource]
