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

// [START healthcare_fhir_execute_bundle]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.CloudHealthcare.Projects.Locations.Datasets.FhirStores.ExecuteBundle;
import com.google.api.services.healthcare.v1beta1.model.HttpBody;
import com.google.gson.Gson;
import java.io.IOException;

public class FhirStoreExecuteBundle {
  private static final Gson GSON = new Gson();

  public static void executeBundle(String fhirStoreName) throws IOException {
    HttpBody context = new HttpBody();
    ExecuteBundle request =
        HealthcareQuickstart.getCloudHealthcareClient()
            .projects()
            .locations()
            .datasets()
            .fhirStores()
            .executeBundle(fhirStoreName, context);
    request.setAccessToken(HealthcareQuickstart.getAccessToken());
    HttpBody response = request.execute();

    System.out.println("Executed FHIR bundle: " + GSON.toJson(response));
  }
}
// [END healthcare_fhir_execute_bundle]
