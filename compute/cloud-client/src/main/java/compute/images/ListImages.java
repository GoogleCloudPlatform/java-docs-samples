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

// [START compute_images_get_list]

import com.google.cloud.compute.v1.Image;
import com.google.cloud.compute.v1.ImagesClient;
import com.google.cloud.compute.v1.ListImagesRequest;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListImages {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project you want to use.
    String projectId = "your-project-id";

    listImages(projectId);
  }

  // Retrieve a list of images available in given project.
  public static List<Image> listImages(String projectId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ImagesClient client = ImagesClient.create()) {
      ListImagesRequest request = ListImagesRequest.newBuilder()
              .setProject(projectId)
              .build();

      ArrayList<Image> images = Lists.newArrayList(client.list(request).iterateAll());

      System.out.printf("'%s' images has been retrieved successfully", images.size());

      return images;
    }
  }
}
// [END compute_images_get_list]