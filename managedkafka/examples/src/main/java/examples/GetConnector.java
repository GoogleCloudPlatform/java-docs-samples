/*
 * Copyright 2025 Google LLC
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

package examples;

// [START managedkafka_get_connector]
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.managedkafka.v1.Connector;
import com.google.cloud.managedkafka.v1.ConnectorName;
import com.google.cloud.managedkafka.v1.ManagedKafkaConnectClient;
import java.io.IOException;

public class GetConnector {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the example.
    String projectId = "my-project-id";
    String region = "my-region"; // e.g. us-east1
    String clusterId = "my-connect-cluster";
    String connectorId = "my-connector";
    getConnector(projectId, region, clusterId, connectorId);
  }

  public static void getConnector(
      String projectId, String region, String clusterId, String connectorId) throws IOException {
    try (ManagedKafkaConnectClient managedKafkaConnectClient = ManagedKafkaConnectClient.create()) {
      ConnectorName name = ConnectorName.of(projectId, region, clusterId, connectorId);
      // This operation is handled synchronously.
      Connector connector = managedKafkaConnectClient.getConnector(name);
      System.out.println(connector.getAllFields());
    } catch (IOException | ApiException e) {
      System.err.printf("managedKafkaConnectClient.getConnector got err: %s\n", e.getMessage());
    }
  }
}
// [END managedkafka_get_connector]
