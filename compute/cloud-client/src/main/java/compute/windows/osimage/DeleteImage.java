// Copyright 2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package compute.windows.osimage;

// [START compute_images_delete]

import com.google.cloud.compute.v1.ImagesClient;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeleteImage {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you use.
    String project = "your-project-id";
    // Name of the image you want to delete.
    String imageName = "your-image-name";

    deleteImage(project, imageName);
  }

  // Deletes a disk image.
  public static void deleteImage(String project, String imageName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `imagesClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (ImagesClient imagesClient = ImagesClient.create()) {
      Operation response = imagesClient.deleteAsync(project, imageName).get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Image deletion failed ! ! " + response);
        return;
      }
      System.out.printf("Operation Status for Image Name %s: %s ", imageName, response.getStatus());
    }
  }
}
// [END compute_images_delete]