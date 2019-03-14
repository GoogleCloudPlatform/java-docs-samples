package com.google.healthcare.datasets;

// [START healthcare_list_datasets]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.model.Dataset;
import com.google.api.services.healthcare.v1beta1.model.ListDatasetsResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class DatasetList {
  public static void listDatasets(String projectId, String cloudRegion) throws IOException {
    cloudRegion = Optional.of(cloudRegion).orElse("us-central1");
    String parentName = String.format("projects/%s/locations/%s", projectId, cloudRegion);
    ListDatasetsResponse response = HealthcareQuickstart.getCloudHealthcareClient()
        .projects()
        .locations()
        .datasets()
        .list(parentName)
        .execute();
    List<Dataset> datasets = response.getDatasets();
    if (datasets == null) {
      System.out.println("Retrieved 0 datasets");
      return;
    }
    System.out.println("Retrieved " + datasets.size() + " datasets");
    for (Dataset dataset : datasets) {
      System.out.println("  - " + dataset.getName());
    }
  }
}
// [END healthcare_list_datasets]
