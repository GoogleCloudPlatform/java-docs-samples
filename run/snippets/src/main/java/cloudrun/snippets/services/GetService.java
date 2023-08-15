/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloudrun.snippets.services;

// [START cloudrun_get_service]
import com.google.cloud.run.v2.Service;
import com.google.cloud.run.v2.ServiceName;
import com.google.cloud.run.v2.ServicesClient;
import java.io.IOException;

public class GetService {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String location = "us-central1";
    String serviceId = "my-service";
    getService(projectId, location, serviceId);
  }

  public static void getService(
      String projectId, String location, String serviceId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ServicesClient servicesClient = ServicesClient.create()) {
      // Define the full name of the Service.
      ServiceName name = ServiceName.of(projectId, location, serviceId);
      // Send the request
      Service response = servicesClient.getService(name);
      // Print example usage of the Service object
      System.out.printf("Service, %s, created by %s.\n", response.getName(), response.getCreator());
      System.out.println("Lastest revision: " + response.getLatestCreatedRevision());
      System.out.println("Latest ready revision: " + response.getLatestReadyRevision());
    }
  }
}
// [END cloudrun_get_service]
