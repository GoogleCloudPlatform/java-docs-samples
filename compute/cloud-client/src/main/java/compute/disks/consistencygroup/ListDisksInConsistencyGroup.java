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
    String consistencyGroupName ="my-group";// "CONSISTENCY_GROUP";
    listDisksInConsistencyGroup(project, location, consistencyGroupName);
  }

  // Lists disks in a consistency group.
  public static List<Disk> listDisksInConsistencyGroup(
      String project, String location, String consistencyGroupName) throws IOException {
    String link = String.format("https://www.googleapis.com/compute/v1/projects/%s/regions/%S/resourcePolicies/%s", project, location, consistencyGroupName);
    String filter = String.format("resource_policies: %s", link);
    // If your disk has zonal location uncomment these lines
    //    try (DisksClient disksClient = DisksClient.create()) {
    //      ListDisksRequest request =
    //          ListDisksRequest.newBuilder()
    //              .setProject(project)
    //              .setZone(location)
    //              .setFilter(filter)
    //              .build();
    //      List<Disk> diskList = new ArrayList<>();
    //      for (Disk disk : disksClient.list(request).iterateAll()) {
    //        diskList.add(disk);
    //      }
    //      System.out.printf("Disks found in consistency group: %s%n", diskList);
    //    }

    try (RegionDisksClient disksClient = RegionDisksClient.create()) {
      ListRegionDisksRequest request =
          ListRegionDisksRequest.newBuilder()
              .setProject(project)
              .setRegion(location)
//              .setFilter(filter)
              .build();
      List<Disk> diskList = new ArrayList<>();
      for (Disk disk : disksClient.list(request).iterateAll()) {
        System.out.println(disk);
        diskList.add(disk);
      }
      return diskList;
    }
  }
}
// [END compute_consistency_group_list_disks]
