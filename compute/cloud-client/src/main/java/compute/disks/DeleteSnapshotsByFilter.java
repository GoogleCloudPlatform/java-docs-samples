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

// [START compute_snapshot_delete_by_filter]

package compute.disks;

import com.google.cloud.compute.v1.ListSnapshotsRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import com.google.cloud.compute.v1.Snapshot;
import com.google.cloud.compute.v1.SnapshotsClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeleteSnapshotsByFilter {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";

    // Filter to be applied when looking for snapshots for deletion. Learn more about filters here:
    // https://cloud.google.com/java/docs/reference/google-cloud-compute/latest/com.google.cloud.compute.v1.ListSnapshotsRequest
    String filter = "FILTER";

    deleteSnapshotsByFilter(projectId, filter);
  }

  // Deletes all snapshots in project that meet the filter criteria.
  public static void deleteSnapshotsByFilter(String projectId, String filter)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
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

      // Iterate through the resultant snapshots and delete them.
      for (Snapshot snapshot : snapshotsClient.list(listSnapshotsRequest).iterateAll()) {
        Operation operation = snapshotsClient.deleteAsync(projectId, snapshot.getName())
            .get(3, TimeUnit.MINUTES);

        if (operation.hasError() || operation.getStatus() != Status.DONE) {
          throw new Error("Snapshot deletion failed!" + operation.getError());
        }
        System.out.printf("Snapshot deleted: %s", snapshot.getName());
      }
    }
  }
}
// [END compute_snapshot_delete_by_filter]