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

package compute.disks;

// [START compute_disk_list]

import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.ListDisksRequest;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class ListDisks {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.

    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";

    // Name of the zone in which to list the disks.
    String zone = "europe-central2-b";

    // Filter to be applied when listing disks. Learn more about filters here:
    // https://cloud.google.com/python/docs/reference/compute/latest/google.cloud.compute_v1.types.ListDisksRequest
    String filter = "FILTER_CONDITION";

    listDisks(projectId, zone, filter);
  }

  // Lists disks from a project.
  public static void listDisks(String projectId, String zone, String filter)
      throws IOException {

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `disksClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (DisksClient disksClient = DisksClient.create()) {

      // Create the request object.
      ListDisksRequest listDisksRequest = ListDisksRequest.newBuilder()
          .setProject(projectId)
          .setZone(zone)
          .setFilter(filter)
          .build();

      for (Disk disk : disksClient.list(listDisksRequest).iterateAll()) {
        System.out.println(disk.getName());
      }
      System.out.println("Listed all disks.");
    }
  }
}
// [END compute_disk_list]