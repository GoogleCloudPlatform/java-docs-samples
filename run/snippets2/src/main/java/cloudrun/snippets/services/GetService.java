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

import java.io.IOException;
// [START cloudrun_Services_GetService_Servicename_sync]
import com.google.cloud.run.v2.Service;
import com.google.cloud.run.v2.ServiceName;
import com.google.cloud.run.v2.ServicesClient;

public class GetService {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "PROJECT";
    String location = "us-central1";
    String serviceName = "my-service";
    syncGetServiceServicename(projectId, location, serviceName);
  }

  public static void syncGetServiceServicename(String projectId, String location, String serviceName) throws IOException {

    try (ServicesClient servicesClient = ServicesClient.create()) {
      ServiceName name = ServiceName.of(projectId, location, serviceName);
      Service service = servicesClient.getService(name);
      System.out.printf("Service, %s, created by %s.\n", service.getName(), service.getCreator());
      System.out.println("Lastest revision: " + service.getLatestCreatedRevision());
      System.out.println("Latest ready revision: " + service.getLatestReadyRevision());
    }
  }
}
// [END cloudrun_Services_GetService_Servicename_sync]
