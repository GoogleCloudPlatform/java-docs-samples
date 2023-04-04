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

// [START compute_snapshot_list]

import com.google.cloud.compute.v1.ListSnapshotsRequest;
import com.google.cloud.compute.v1.Snapshot;
import com.google.cloud.compute.v1.SnapshotsClient;
import java.io.IOException;

public class ListSnapshots {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.

    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";

    // Filter to be applied when listing snapshots. Learn more about filters here:
    // https://cloud.google.com/python/docs/reference/compute/latest/google.cloud.compute_v1.types.ListSnapshotsRequest
    String filter = "FILTER_CONDITION";

    listSnapshots(projectId, filter);
  }

  // List snapshots from a project.
  public static void listSnapshots(String projectId, String filter) throws IOException {

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `snapshotsClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (SnapshotsClient snapshotsClient = SnapshotsClient.create()) {

      // Create the List Snapshot request.
      ListSnapshotsRequest listSnapshotsRequest = ListSnapshotsRequest.newBuilder()
          .setProject(projectId)
          .setFilter(filter)
          .build();

      System.out.println("List of snapshots:");
      for (Snapshot snapshot : snapshotsClient.list(listSnapshotsRequest).iterateAll()) {
        System.out.println(snapshot.getName());
      }
    }
  }
}
// [END compute_snapshot_list]