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

// [START compute_snapshot_delete]

import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.SnapshotsClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeleteSnapshot {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.

    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";

    // Name of the snapshot to be deleted.
    String snapshotName = "YOUR_SNAPSHOT_NAME";

    deleteSnapshot(projectId, snapshotName);
  }

  // Delete a snapshot of a disk.
  public static void deleteSnapshot(String projectId, String snapshotName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `snapshotsClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (SnapshotsClient snapshotsClient = SnapshotsClient.create()) {

      Operation operation = snapshotsClient.deleteAsync(projectId, snapshotName)
          .get(3, TimeUnit.MINUTES);

      if (operation.hasError()) {
        System.out.println("Snapshot deletion failed!" + operation);
        return;
      }

      System.out.println("Snapshot deleted!");
    }
  }
}
// [END compute_snapshot_delete]