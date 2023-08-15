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
import java.util.concurrent.ExecutionException;
// [START cloudrun_Services_CreateService_LRO_async]
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.run.v2.CreateServiceRequest;
import com.google.cloud.run.v2.LocationName;
import com.google.cloud.run.v2.Service;
import com.google.cloud.run.v2.ServicesClient;

public class AsyncCreateServiceLRO {

  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "PROJECT";
    String location = "us-central1";
    asyncCreateServiceLRO(projectId, location);
  }

  public static void asyncCreateServiceLRO(String projectId, String location) throws IOException, InterruptedException, ExecutionException {
    try (ServicesClient servicesClient = ServicesClient.create()) {
      CreateServiceRequest request =
          CreateServiceRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .setService(Service.newBuilder().build())
              .setServiceId("serviceId-194185552")
              .setValidateOnly(true)
              .build();
      OperationFuture<Service, Service> future =
          servicesClient.createServiceOperationCallable().futureCall(request);
      // Do something.
      Service response = future.get();
    }
  }
}
// [END cloudrun_Services_CreateService_LRO_async]
