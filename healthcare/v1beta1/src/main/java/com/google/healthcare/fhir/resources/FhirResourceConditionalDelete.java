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

// [START healthcare_conditional_delete_resource]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.CloudHealthcare.Projects.Locations.Datasets.FhirStores.Fhir.ConditionalDeleteResource;
import java.io.IOException;
import java.util.Optional;

public class FhirResourceConditionalDelete {
  public static void conditionalDeleteFhirResource(
      String projectId,
      String cloudRegion,
      String datasetId,
      String fhirStoreId,
      String resourceType)
      throws IOException {
    String parentName =
        String.format(
            "projects/%s/locations/%s/datasets/%s/fhirStores/%s",
            projectId, Optional.of(cloudRegion).orElse("us-central1"), datasetId, fhirStoreId);
    ConditionalDeleteResource request =
        HealthcareQuickstart.getCloudHealthcareClient()
            .projects()
            .locations()
            .datasets()
            .fhirStores()
            .fhir()
            .conditionalDeleteResource(parentName, resourceType);
    request.setAccessToken(HealthcareQuickstart.getAccessToken());
    request.execute();
    System.out.println("Conditionally deleted FHIR resource: " + resourceType);
  }
}
// [END healthcare_conditional_delete_resource]
