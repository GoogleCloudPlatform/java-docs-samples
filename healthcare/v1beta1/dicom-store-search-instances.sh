curl -X GET \
     -H "Authorization: Bearer "$(gcloud auth print-access-token) \
     -H "Content-Type: application/dicom+json; charset=utf-8" \
     "https://healthcare.googleapis.com/v1beta1/projects/dzlier-work/locations/us-central1/datasets/dataset-13c3/dicomStores/dicom-store-13c3/dicomWeb/instances"