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

// [START compute_images_get]

import com.google.cloud.compute.v1.GetImageRequest;
import com.google.cloud.compute.v1.Image;
import com.google.cloud.compute.v1.ImagesClient;
import java.io.IOException;

public class GetImage {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project you want to use.
    String projectId = "your-project-id";
    // Name of the image you want to retrieve.
    String imageName = "your-image-name";

    getImage(projectId, imageName);
  }

  // Retrieve detailed information about a single image from a project
  public static Image getImage(String projectId, String imageName) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ImagesClient client = ImagesClient.create()) {
      GetImageRequest request = GetImageRequest.newBuilder()
              .setProject(projectId)
              .setImage(imageName)
              .build();

      Image image = client.get(request);

      System.out.printf("Image '%s' has been retrieved successfully", image.getName());

      return image;
    }
  }
}
// [END compute_images_get]