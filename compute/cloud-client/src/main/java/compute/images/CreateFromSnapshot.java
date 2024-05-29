/*
 * Copyright 2024 Google LLC
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

package compute.images;

// [START compute_images_create_from_snapshot]

import com.google.cloud.compute.v1.GuestOsFeature;
import com.google.cloud.compute.v1.Image;
import com.google.cloud.compute.v1.ImagesClient;
import com.google.cloud.compute.v1.InsertImageRequest;
import com.google.cloud.compute.v1.Snapshot;
import com.google.cloud.compute.v1.SnapshotsClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateFromSnapshot {
  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "your-project-id";
    // name of the snapshot you want to use as a base of your image.
    String sourceSnapshotName = "your-snapshot-name";
    // name of the image you want to create.
    String imageName = "your-image-name";
    // name of the project that hosts the source image. If left unset, it's assumed to equal
    // the `project_id`.
    String sourceProjectId = "your-source-project-id";
    // an iterable collection of guest features you want to enable for the bootable image.
    // Learn more about Guest OS features here:
    // https://cloud.google.com/compute/docs/images/
    // create-delete-deprecate-private-images#guest-os-features
    List<String> guestOsFeature = new ArrayList<>();
    // the storage location of your image. For example, specify "us" to store the image in the
    // `us` multi-region, or "us-central1" to store it in the `us-central1` region.
    // If you do not make a selection,
    // Compute Engine stores the image in the multi-region closest to your image's source location.
    String storageLocation = "your-storage-location";

    createImageFromImage(projectId, sourceSnapshotName, imageName,
            sourceProjectId, guestOsFeature, storageLocation);
  }

  // Creates an image based on a snapshot.
  public static Image createImageFromImage(String projectId, String sourceSnapshotName,
                                           String imageName, String sourceProjectId,
                                           List<String> guestOsFeature, String storageLocation)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    if (sourceProjectId == null) {
      sourceProjectId = projectId;
    }
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ImagesClient imagesClient = ImagesClient.create();
         SnapshotsClient snapshotsClient = SnapshotsClient.create()) {
      Snapshot snapshot = snapshotsClient.get(sourceProjectId, sourceSnapshotName);

      Image.Builder imageResource = Image.newBuilder()
              .setName(imageName)
              .setSourceSnapshot(snapshot.getSelfLink());
      if (storageLocation != null) {
        imageResource.addStorageLocations(storageLocation);
      }
      if (guestOsFeature != null) {
        for (String feature : guestOsFeature) {
          GuestOsFeature build = GuestOsFeature.newBuilder()
                  .setType(feature)
                  .build();
          imageResource.addGuestOsFeatures(build);
        }
      }
      InsertImageRequest request = InsertImageRequest.newBuilder()
              .setProject(projectId)
              .setRequestId(UUID.randomUUID().toString())
              .setImageResource(imageResource)
              .build();
      imagesClient.insertCallable().futureCall(request).get(60, TimeUnit.SECONDS);

      return imagesClient.get(projectId, imageName);
    }
  }
}
// [END compute_images_create_from_snapshot]