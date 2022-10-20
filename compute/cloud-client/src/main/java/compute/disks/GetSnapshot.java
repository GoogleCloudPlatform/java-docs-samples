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

// [START compute_snapshot_get]

package compute.disks;

import com.google.cloud.compute.v1.Snapshot;
import com.google.cloud.compute.v1.SnapshotsClient;
import java.io.IOException;

public class GetSnapshot {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";

    // Name of the snapshot to look up.
    String snapshotName = "YOUR_SNAPSHOT_NAME";

    getSnapshot(projectId, snapshotName);
  }

  // Get information about a snapshot.
  public static void getSnapshot(String projectId, String snapshotName) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `snapshotsClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (SnapshotsClient snapshotsClient = SnapshotsClient.create()) {
      Snapshot snapshot = snapshotsClient.get(projectId, snapshotName);
      System.out.printf("Retrieved the snapshot: %s", snapshot.getName());
    }
  }
}
// [END compute_snapshot_get]