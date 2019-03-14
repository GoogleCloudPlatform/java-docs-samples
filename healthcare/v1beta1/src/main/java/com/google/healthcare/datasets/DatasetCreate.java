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

package com.google.healthcare.datasets;

// [START healthcare_create_dataset]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.CloudHealthcare.Projects.Locations.Datasets.Create;
import com.google.api.services.healthcare.v1beta1.model.Dataset;

import java.io.IOException;
import java.util.Optional;

public class DatasetCreate {
  public static void createDataset(String projectId, String cloudRegion, String datasetId)
      throws IOException {
    Dataset dataset = new Dataset();
    cloudRegion = Optional.of(cloudRegion).orElse("us-central1");

    String parentName = String.format("projects/%s/locations/%s", projectId, cloudRegion);
    Create createRequest = HealthcareQuickstart.getCloudHealthcareClient()
        .projects()
        .locations()
        .datasets()
        .create(parentName, dataset);

    createRequest.setDatasetId(datasetId);
    createRequest.execute();

    System.out.println("Created Dataset: " + createRequest.getDatasetId());
  }
}
// [END healthcare_create_dataset]
