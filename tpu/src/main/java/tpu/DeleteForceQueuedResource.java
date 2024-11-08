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

package tpu;

//[START tpu_queued_resources_delete_force]
import com.google.api.gax.retrying.RetrySettings;
import com.google.cloud.tpu.v2alpha1.DeleteQueuedResourceRequest;
import com.google.cloud.tpu.v2alpha1.TpuClient;
import com.google.cloud.tpu.v2alpha1.TpuSettings;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.threeten.bp.Duration;

public class DeleteForceQueuedResource {
  private final TpuClient tpuClient;

  // Constructor to inject the TpuClient
  public DeleteForceQueuedResource(TpuClient tpuClient) {
    this.tpuClient = tpuClient;
  }

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project.
    String projectId = "YOUR_PROJECT_ID";
    // The zone in which the TPU was created.
    String zone = "europe-west4-a";
    // The name for your Queued Resource.
    String queuedResourceId = "QUEUED_RESOURCE_ID";

    TpuClient client = TpuClient.create(getSettings());
    DeleteForceQueuedResource creator = new DeleteForceQueuedResource(client);

    creator.deleteForceQueuedResource(projectId, zone, queuedResourceId);
  }

  // Deletes a Queued Resource asynchronously with --force flag.
  public void deleteForceQueuedResource(String projectId, String zone, String queuedResourceId)
      throws ExecutionException, InterruptedException {
    String name = String.format("projects/%s/locations/%s/queuedResources/%s",
        projectId, zone, queuedResourceId);
    DeleteQueuedResourceRequest request =
        DeleteQueuedResourceRequest.newBuilder().setName(name).setForce(true).build();

    this.tpuClient.deleteQueuedResourceAsync(request).get();

    System.out.printf("Deleted Queued Resource: %s\n", name);
  }

  // With these settings the client library handles the Operation's polling mechanism
  // and prevent CancellationException error
  private static TpuSettings getSettings() throws IOException {
    TpuSettings.Builder clientSettings =
        TpuSettings.newBuilder();
    clientSettings
        .deleteQueuedResourceSettings()
        .setRetrySettings(
            RetrySettings.newBuilder()
                .setInitialRetryDelay(Duration.ofMillis(5000L))
                .setRetryDelayMultiplier(2.0)
                .setInitialRpcTimeout(Duration.ZERO)
                .setRpcTimeoutMultiplier(1.0)
                .setMaxRetryDelay(Duration.ofMillis(45000L))
                .setTotalTimeout(Duration.ofHours(24L))
                .build());

    return clientSettings.build();
  }
}
//[END tpu_queued_resources_delete_force]
