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

// [START compute_consistency_group_list_disks]
import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.ListRegionDisksRequest;
import com.google.cloud.compute.v1.RegionDisksClient;
// If your disk has zonal location uncomment these lines
//import com.google.cloud.compute.v1.ListDisksRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListDisksInConsistencyGroup {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // The project that contains the disk.
    String project = "tyaho-softserve-project";//"YOUR_PROJECT_ID";
    // The zone or region of the disk.
    String location = "us-central1";
    // The name of the consistency group.
    String consistencyGroupName = "my-group";//"CONSISTENCY_GROUP";
    // The region of the consistency group.
    String consistencyGroupLocation = "us-central1";
    listDisksInConsistencyGroup(project, location, consistencyGroupName, consistencyGroupLocation);
  }

  // Lists disks in a consistency group.
  public static List<Disk> listDisksInConsistencyGroup(String project, String location,
                                                       String consistencyGroupName, String consistencyGroupLocation) throws IOException {
    List<Disk> diskList = new ArrayList<>();
    String filter = String.format("https://www.googleapis.com/compute/v1/projects/%s/regions/%s/resourcePolicies/%s",
        project, consistencyGroupLocation, consistencyGroupName);
//    https://www.googleapis/.com/compute/v1/projects/tyaho-softserve-project/regions/us-central1/resourcePolicies/my-group
    // If your disk has zonal location uncomment these lines
    // try (DisksClient disksClient = DisksClient.create()) {
    //    ListDisksRequest request =
    //    ListDisksRequest.newBuilder()
    //        .setProject(project)
    //        .setZone(location)
    //        .build();
    // List<Disk> diskList = new ArrayList<>();
    // for (Disk disk : disksClient.list(request).iterateAll()) {
    //      diskList.add(disk);
    //   }
    //  }

    // Filtering must be done manually for now, since list filtering
    // inside disksClient.list is not supported yet.
    try (RegionDisksClient disksClient = RegionDisksClient.create()) {
      ListRegionDisksRequest request =
          ListRegionDisksRequest.newBuilder()
              .setProject(project)
              .setRegion(location)
              .build();
//      RegionDisksClient.ListPagedResponse response = disksClient.list(request);

      for (Disk disk : disksClient.list(request).iterateAll()) { // Use regional disks client and request for regional disks if needed
        if (disk.getResourcePoliciesList().contains(filter)) {
          diskList.add(disk);
        }
      }
      System.out.println(diskList);
      return diskList;
    }
  }
}
// [END compute_consistency_group_list_disks]
