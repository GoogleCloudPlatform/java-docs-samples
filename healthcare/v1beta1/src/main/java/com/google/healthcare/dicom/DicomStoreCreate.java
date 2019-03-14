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
      String projectId,
      String cloudRegion,
      String datasetId,
      String dicomStoreId) throws IOException {
    String parentName = String.format(
        "projects/%s/locations/%s/datasets/%s",
        projectId,
        cloudRegion,
        datasetId);
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
