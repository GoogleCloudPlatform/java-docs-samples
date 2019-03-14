$cred = gcloud auth print-access-token
$headers = @{ Authorization = "Bearer $cred" }

Invoke-RestMethod `
  -Method Get `
  -Headers $headers `
  -ContentType: "application/json; charset=utf-8" `
  -Uri "https://healthcare.googleapis.com/v1alpha1/projects/dzlier-work/locations/us-central1/datasets/dataset-13c3/dicomStores/dicom-store-13c3/dicomWeb/instances"