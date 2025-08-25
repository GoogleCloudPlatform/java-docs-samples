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

// [START managedkafka_update_connector]
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.managedkafka.v1.Connector;
import com.google.cloud.managedkafka.v1.ConnectorName;
import com.google.cloud.managedkafka.v1.ManagedKafkaConnectClient;
import com.google.protobuf.FieldMask;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UpdateConnector {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the example.
    String projectId = "my-project-id";
    String region = "my-region"; // e.g. us-east1
    String clusterId = "my-connect-cluster";
    String connectorId = "my-connector";
    // The new value for the 'tasks.max' configuration.
    String maxTasks = "5";
    updateConnector(projectId, region, clusterId, connectorId, maxTasks);
  }

  public static void updateConnector(
      String projectId, String region, String clusterId, String connectorId, String maxTasks)
      throws IOException {
    try (ManagedKafkaConnectClient managedKafkaConnectClient = ManagedKafkaConnectClient.create()) {
      Map<String, String> configMap = new HashMap<>();
      configMap.put("tasks.max", maxTasks);

      Connector connector =
          Connector.newBuilder()
              .setName(ConnectorName.of(projectId, region, clusterId, connectorId).toString())
              .putAllConfigs(configMap)
              .build();

      // The field mask specifies which fields to update. Here, we update the 'config' field.
      FieldMask updateMask = FieldMask.newBuilder().addPaths("config").build();

      // This operation is handled synchronously.
      Connector updatedConnector = managedKafkaConnectClient.updateConnector(connector, updateMask);
      System.out.printf("Updated connector: %s\n", updatedConnector.getName());
      System.out.println(updatedConnector.getAllFields());

    } catch (IOException | ApiException e) {
      System.err.printf("managedKafkaConnectClient.updateConnector got err: %s\n", e.getMessage());
    }
  }
}
// [END managedkafka_update_connector]
