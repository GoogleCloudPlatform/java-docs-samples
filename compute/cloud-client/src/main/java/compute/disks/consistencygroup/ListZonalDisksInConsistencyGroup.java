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

package compute.disks.consistencygroup;

// [START compute_consistency_group_list_disks_zonal]
import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.ListDisksRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ListZonalDisksInConsistencyGroup {
  public static void main(String[] args)
          throws IOException, InterruptedException, ExecutionException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String project = "YOUR_PROJECT_ID";
    // Name of the consistency group.
    String consistencyGroupName = "CONSISTENCY_GROUP_ID";
    // Zone of the disk.
    String disksLocation = "us-central1-a";
    // Region of the consistency group.
    String consistencyGroupLocation = "us-central1";

    listZonalDisksInConsistencyGroup(
            project, consistencyGroupName, consistencyGroupLocation, disksLocation);
  }

  // Lists disks in a consistency group.
  public static List<Disk> listZonalDisksInConsistencyGroup(String project,
      String consistencyGroupName, String consistencyGroupLocation, String disksLocation)
          throws IOException {
    String filter = String
            .format("https://www.googleapis.com/compute/v1/projects/%s/regions/%s/resourcePolicies/%s",
                    project, consistencyGroupLocation, consistencyGroupName);
    List<Disk> disksList = new ArrayList<>();
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (DisksClient disksClient = DisksClient.create()) {
      ListDisksRequest request =
              ListDisksRequest.newBuilder()
                      .setProject(project)
                      .setZone(disksLocation)
                      .build();
      DisksClient.ListPagedResponse response = disksClient.list(request);

      for (Disk disk : response.iterateAll()) {
        if (disk.getResourcePoliciesList().contains(filter)) {
          disksList.add(disk);
        }
      }
    }
    System.out.println(disksList.size());
    return disksList;
  }
}
// [END compute_consistency_group_list_disks_zonal]
