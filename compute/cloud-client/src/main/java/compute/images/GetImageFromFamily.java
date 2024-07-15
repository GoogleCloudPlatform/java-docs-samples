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

// [START compute_images_get_from_family]

import com.google.cloud.compute.v1.GetFromFamilyImageRequest;
import com.google.cloud.compute.v1.Image;
import com.google.cloud.compute.v1.ImagesClient;
import java.io.IOException;

public class GetImageFromFamily {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project you want to use.
    String projectId = "debian-cloud";
    // Name of the image family you want to retrieve the image from.
    // List of public operating system (OS) images:
    // https://cloud.google.com/compute/docs/images/os-details
    String family = "debian-11";

    getImageFromFamily(projectId, family);
  }

  // Retrieve the newest image that is part of a given family in a project.
  public static Image getImageFromFamily(String projectId, String family) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ImagesClient client = ImagesClient.create()) {
      GetFromFamilyImageRequest request = GetFromFamilyImageRequest.newBuilder()
              .setProject(projectId)
              .setFamily(family)
              .build();

      Image image = client.getFromFamily(request);

      System.out.printf("Image '%s' has been retrieved successfully", image.getName());

      return image;
    }
  }
}
// [END compute_images_get_from_family]