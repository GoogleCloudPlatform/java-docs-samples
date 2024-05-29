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

// [START compute_images_set_deprecation_status]

import com.google.cloud.compute.v1.DeprecateImageRequest;
import com.google.cloud.compute.v1.DeprecationStatus;
import com.google.cloud.compute.v1.Image;
import com.google.cloud.compute.v1.ImagesClient;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SetImageDeprecationStatus {
  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "your-project-id";
    // name of the image you want to create.
    String imageName = "your-image-name";
    // name of the image you want to create.
    DeprecationStatus.State status = DeprecationStatus.State.DEPRECATED;

    setDeprecationStatus(projectId, imageName, status);
  }

  // Modify the deprecation status of an image.
  public static Image setDeprecationStatus(String projectId, String imageName,
                                           DeprecationStatus.State status)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ImagesClient client = ImagesClient.create()) {
      DeprecationStatus deprecationStatusResource = DeprecationStatus.newBuilder()
              .setState(status.name())
              .build();
      DeprecateImageRequest request = DeprecateImageRequest.newBuilder()
              .setProject(projectId)
              .setImage(imageName)
              .setDeprecationStatusResource(deprecationStatusResource)
              .setRequestId(UUID.randomUUID().toString())
              .build();

      client.deprecateCallable().futureCall(request).get(60, TimeUnit.SECONDS);

      return client.get(projectId, imageName);
    }
  }
}
// [END compute_images_set_deprecation_status]