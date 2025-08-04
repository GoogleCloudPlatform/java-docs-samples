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

// [START managedkafka_resume_connector]
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.managedkafka.v1.ConnectorName;
import com.google.cloud.managedkafka.v1.ManagedKafkaConnectClient;
import com.google.cloud.managedkafka.v1.ResumeConnectorRequest;
import java.io.IOException;

public class ResumeConnector {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the example.
    String projectId = "my-project-id";
    String region = "my-region"; // e.g. us-east1
    String connectClusterId = "my-connect-cluster";
    String connectorId = "my-connector";
    resumeConnector(projectId, region, connectClusterId, connectorId);
  }

  public static void resumeConnector(
      String projectId, String region, String connectClusterId, String connectorId)
      throws Exception {
    try (ManagedKafkaConnectClient managedKafkaConnectClient = 
        ManagedKafkaConnectClient.create()) {
      ConnectorName connectorName = ConnectorName.of(projectId, region, connectClusterId, 
          connectorId);
      ResumeConnectorRequest request = ResumeConnectorRequest.newBuilder()
          .setName(connectorName.toString()).build();

      // This operation is being handled synchronously.
      managedKafkaConnectClient.resumeConnector(request);
      System.out.printf("Connector %s resumed successfully.\n", connectorId);
    } catch (IOException | ApiException e) {
      System.err.printf("managedKafkaConnectClient.resumeConnector got err: %s", 
          e.getMessage());
    }
  }
}

// [END managedkafka_resume_connector]
