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
 *
 *
 * Create a featurestore resource to contain entity types and features. See
 * https://cloud.google.com/vertex-ai/docs/featurestore/setup before running
 * the code snippet
 */

package aiplatform;

// [START aiplatform_create_featureOnlineStore_bigtable_sample]

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.aiplatform.v1beta1.*;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateFeatureOnlineStoreFixedNodesSample {

  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    String project = "YOUR_PROJECT_ID";
    String featureOnlineStoreId = "YOUR_FEATURESTORE_ID";
    int minNodeCount = 1;
    int maxNodeCount = 2;
    String location = "us-central1";
    String endpoint = "us-central1-aiplatform.googleapis.com:443";
    int timeout = 900;
    createFeatureOnlineStoreFixedNodesSample(
        project, featureOnlineStoreId, minNodeCount, maxNodeCount, location, endpoint, timeout);
  }

  static void createFeatureOnlineStoreFixedNodesSample(
      String project,
      String featureOnlineStoreId,
      int minNodeCount,
      int maxNodeCount,
      String location,
      String endpoint,
      int timeout)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {

    FeatureOnlineStoreAdminServiceSettings featureOnlineStoreAdminServiceSettings =
        FeatureOnlineStoreAdminServiceSettings.newBuilder().setEndpoint(endpoint).build();

    try (FeatureOnlineStoreAdminServiceClient featureOnlineStoreAdminServiceClient =
        FeatureOnlineStoreAdminServiceClient.create(featureOnlineStoreAdminServiceSettings)) {

      FeatureOnlineStore.Bigtable.Builder builderValue =
          FeatureOnlineStore.Bigtable.newBuilder()
              .setAutoScaling(
                  FeatureOnlineStore.Bigtable.AutoScaling.newBuilder()
                      .setMinNodeCount(minNodeCount)
                      .setMaxNodeCount(maxNodeCount)
                      .setCpuUtilizationTarget(60));
      FeatureOnlineStore featureOnlineStore =
          FeatureOnlineStore.newBuilder().setBigtable(builderValue).build();

      CreateFeatureOnlineStoreRequest createFeatureOnlineStoreRequest =
          CreateFeatureOnlineStoreRequest.newBuilder()
              .setParent(LocationName.of(project, location).toString())
              .setFeatureOnlineStore(featureOnlineStore)
              .setFeatureOnlineStoreId(featureOnlineStoreId)
              .build();

      OperationFuture<FeatureOnlineStore, CreateFeatureOnlineStoreOperationMetadata>
          featureOnlineStoreFuture =
              featureOnlineStoreAdminServiceClient.createFeatureOnlineStoreAsync(
                  createFeatureOnlineStoreRequest);
      System.out.format(
          "Operation name: %s%n", featureOnlineStoreFuture.getInitialFuture().get().getName());
      System.out.println("Waiting for operation to finish...");
      FeatureOnlineStore featureOnlineStoreResponse =
          featureOnlineStoreFuture.get(timeout, TimeUnit.SECONDS);
      System.out.println("Create FeatureOnlineStore Response");
      System.out.format("Name: %s%n", featureOnlineStoreResponse.getName());
    }
  }
}

// [END aiplatform_create_featureOnlineStore_bigtable_sample]
