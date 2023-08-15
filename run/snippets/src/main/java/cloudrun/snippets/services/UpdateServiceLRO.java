/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package cloudrun.snippets.services;

// [START cloudrun_update_service_LRO]
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.run.v2.Container;
import com.google.cloud.run.v2.EnvVar;
import com.google.cloud.run.v2.RevisionTemplate;
import com.google.cloud.run.v2.Service;
import com.google.cloud.run.v2.ServiceName;
import com.google.cloud.run.v2.ServicesClient;
import com.google.cloud.run.v2.UpdateServiceRequest;
import com.google.protobuf.Timestamp;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class UpdateServiceLRO {

  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String location = "us-central1";
    String serviceId = "my-service-id";
    String imageUrl = "us-docker.pkg.dev/cloudrun/container/hello";
    updateServiceLRO(projectId, location, serviceId, imageUrl);
  }

  public static void updateServiceLRO(String projectId, String location, String serviceId, String imageUrl)
      throws IOException, InterruptedException, ExecutionException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ServicesClient servicesClient = ServicesClient.create()) {
      Service service =
          Service.newBuilder()
              .setName(ServiceName.of(projectId, location, serviceId).toString())
              .setTemplate(
                  RevisionTemplate.newBuilder()
                      .addContainers(Container.newBuilder().setImage(imageUrl)))
              .build();

      UpdateServiceRequest request =
          UpdateServiceRequest.newBuilder().setService(service).setAllowMissing(true).build();
      OperationFuture<Service, Service> future =
          servicesClient.updateServiceOperationCallable().futureCall(request);
      // Do something.
      Service response = future.get();
      System.out.println("Updated service: " + response.getName());
      Timestamp ts = service.getUpdateTime();
      Instant instant = Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
      System.out.println("Updated at: " + instant.toString());
    }
  }
}
// [END cloudrun_update_service_LRO]
