/*
 * Copyright 2019 Google LLC
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

// [START dataproc_list_clusters]
import com.google.cloud.dataproc.v1.Cluster;
import com.google.cloud.dataproc.v1.ClusterControllerClient;
import com.google.cloud.dataproc.v1.ClusterControllerSettings;
import java.io.IOException;

public class ListClusters {

  public static void listClusters(String projectId, String region) throws IOException {
    String myEndpoint = region + "-dataproc.googleapis.com:443";

    // Configure the settings for the cluster controller client
    ClusterControllerSettings clusterControllerSettings =
        ClusterControllerSettings.newBuilder().setEndpoint(myEndpoint).build();

    // Create a cluster controller client with the configured settings. The client only needs to be
    // created once and can be reused for multiple requests. Using a try-with-resources
    // closes the client, but this can also be done manually with the .close() method.
    try (ClusterControllerClient clusterControllerClient =
        ClusterControllerClient.create(clusterControllerSettings)) {

      for (Cluster element : clusterControllerClient.listClusters(projectId, region).iterateAll()) {
        System.out.println(element.getClusterName() + ": " + element.getStatus().getState());
      }
    } catch (IOException e) {
      // Likely this would occur due to issues authenticating with GCP. Make sure the environment
      // variable GOOGLE_APPLICATION_CREDENTIALS is configured.
      System.out.println("Error deleting the cluster controller client: \n" + e.getMessage());
    }
  }
}
// [END dataproc_list_clusters]
