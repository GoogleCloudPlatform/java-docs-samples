/*
 * Copyright 2023 Google LLC
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

package compute.disks;

// [START compute_disk_resize]

import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.DisksResizeRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.ResizeDiskRequest;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResizeDisk {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "your-project-id";

    // Full or partial URL of the disk that you want to resize.
    // This value uses the following format:
    //   * projects/{project_name}/zones/{zone}/disks/{disk_name}
    String diskLink = String.format("projects/%s/zones/%s/disks/%s",
        "project", "zone", "disk");

    // The new size you want to set for the disk in gigabytes.
    int newSizeGb = 23;

    resizeDisk(projectId, diskLink, newSizeGb);
  }

  // Resizes a persistent disk to a specified size in GB. After you resize the disk, you must
  // also resize the file system so that the operating system can access the additional space.
  public static void resizeDisk(String projectId, String diskLink, int newSizeGb)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `disksClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (DisksClient disksClient = DisksClient.create()) {

      Matcher matcher = Pattern.compile("projects/[\\w_-]+/zones/[\\w_-]+/disks/[\\w_-]+")
          .matcher(diskLink);

      String[] match = new String[0];
      while (matcher.find()) {
        match = matcher.group().split("/");
      }

      ResizeDiskRequest resizeDiskRequest = ResizeDiskRequest.newBuilder()
          .setZone(match[3])
          .setDisksResizeRequestResource(DisksResizeRequest.newBuilder()
              .setSizeGb(newSizeGb)
              .build())
          .setDisk(match[5])
          .setProject(projectId)
          .build();

      Operation response = disksClient.resizeAsync(resizeDiskRequest)
          .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Resize disk failed! " + response);
        return;
      }
      System.out.println("Resize disk - operation status: " + response.getStatus());
    }
  }
}
// [END compute_disk_resize]
