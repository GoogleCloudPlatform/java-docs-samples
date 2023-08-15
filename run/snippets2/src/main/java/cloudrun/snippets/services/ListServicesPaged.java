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

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import com.google.api.core.ApiFuture;
// [START cloudrun_Services_ListServices_Paged_async]
import com.google.cloud.run.v2.ListServicesRequest;
import com.google.cloud.run.v2.ListServicesResponse;
import com.google.cloud.run.v2.LocationName;
import com.google.cloud.run.v2.Service;
import com.google.cloud.run.v2.ServicesClient;
import com.google.common.base.Strings;
import com.google.protobuf.Timestamp;

public class ListServicesPaged {

  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "PROJECT";
    String location = "us-central1";
    asyncListServicesPaged(projectId, location);
  }

  public static void asyncListServicesPaged(String projectId, String location) throws IOException, InterruptedException, ExecutionException {
  try (ServicesClient servicesClient = ServicesClient.create()) {
      ListServicesRequest request =
          ListServicesRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .setPageSize(1)
              .setShowDeleted(true)
              .build();

      while (true) {
        ApiFuture<ServicesClient.ListServicesPagedResponse> future = servicesClient.listServicesPagedCallable().futureCall(request);
        for (Service service : future.get().iterateAll()) {
          System.out.println("Service: " + service.getName());
          Timestamp ts = service.getCreateTime();
          Instant instant = Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
          System.out.println("Created at: " + instant.toString());
        }
        String nextPageToken = future.get().getNextPageToken();
        if (!Strings.isNullOrEmpty(nextPageToken)) {
          request = request.toBuilder().setPageToken(nextPageToken).build();
          System.out.println("NEXT");
        } else {
          break;
        }
      }
    }
  }
}
// [END cloudrun_Services_ListServices_Paged_async]
