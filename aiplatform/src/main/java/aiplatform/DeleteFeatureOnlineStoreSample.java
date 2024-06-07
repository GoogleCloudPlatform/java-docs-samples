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

// [START aiplatform_delete_feature_online_store_sample]
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.aiplatform.v1.DeleteFeatureOnlineStoreRequest;
import com.google.cloud.aiplatform.v1.DeleteOperationMetadata;
import com.google.cloud.aiplatform.v1.FeatureOnlineStoreAdminServiceClient;
import com.google.cloud.aiplatform.v1.FeatureOnlineStoreAdminServiceSettings;
import com.google.cloud.aiplatform.v1.FeatureOnlineStoreName;
import com.google.protobuf.Empty;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeleteFeatureOnlineStoreSample {
  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    String project = "YOUR_PROJECT_ID";
    String featureOnlineStoreId = "YOUR_FEATURESTORE_ID";
    boolean useForce = true;
    String location = "us-central1";
    String endpoint = location + "-aiplatform.googleapis.com:443";
    int timeout = 60; // seconds to wait the response

    deleteFeatureOnlineStoreSample(
        project, featureOnlineStoreId, useForce, location, endpoint, timeout);
  }

  // [START aiplatform_delete_feature_online_store_sample_delete]
  static void deleteFeatureOnlineStoreSample(
      String project,
      String featureOnlineStoreId,
      boolean useForce,
      String location,
      String endpoint,
      int timeout)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {

    FeatureOnlineStoreAdminServiceSettings featureOnlineStoreAdminServiceSettings =
        FeatureOnlineStoreAdminServiceSettings.newBuilder().setEndpoint(endpoint).build();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (FeatureOnlineStoreAdminServiceClient featureOnlineStoreAdminServiceClient =
        FeatureOnlineStoreAdminServiceClient.create(featureOnlineStoreAdminServiceSettings)) {

      DeleteFeatureOnlineStoreRequest deleteFeatureOnlineStoreRequest =
          DeleteFeatureOnlineStoreRequest.newBuilder()
              .setName(
                  FeatureOnlineStoreName.of(project, location, featureOnlineStoreId).toString())
              .setForce(useForce)
              .build();

      OperationFuture<Empty, DeleteOperationMetadata> operationFuture =
          featureOnlineStoreAdminServiceClient.deleteFeatureOnlineStoreAsync(
              deleteFeatureOnlineStoreRequest);
      operationFuture.get(timeout, TimeUnit.SECONDS);
    }
  }
  // [END aiplatform_delete_feature_online_store_sample_delete]
}

// [END aiplatform_delete_feature_online_store_sample]
