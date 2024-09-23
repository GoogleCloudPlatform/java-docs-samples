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
 */

package discoveryengine.v1;

// [START genappbuilder_create_data_store]

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.discoveryengine.v1.CollectionName;
import com.google.cloud.discoveryengine.v1.CreateDataStoreMetadata;
import com.google.cloud.discoveryengine.v1.CreateDataStoreRequest;
import com.google.cloud.discoveryengine.v1.DataStore;
import com.google.cloud.discoveryengine.v1.DataStore.ContentConfig;
import com.google.cloud.discoveryengine.v1.DataStoreServiceClient;
import com.google.cloud.discoveryengine.v1.DataStoreServiceSettings;
import com.google.cloud.discoveryengine.v1.IndustryVertical;
import com.google.cloud.discoveryengine.v1.SolutionType;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class CreateDataStore {
  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "PROJECT_ID";
    // Location of the data store. Options: "global", "us", "eu"
    String location = "global";
    // Data store ID.
    String dataStoreId = "DATA_STORE_ID";
    createDataStore(projectId, location, dataStoreId);
  }

  /** Create a DataStore. */
  public static void createDataStore(String projectId, String location, String dataStoreId)
      throws IOException, ExecutionException, InterruptedException {
    // For more information, refer to:
    // https://cloud.google.com/generative-ai-app-builder/docs/locations#specify_a_multi-region_for_your_data_store
    String endpoint = (location.equals("global")) 
        ? String.format("discoveryengine.googleapis.com:443", location) 
        : String.format("%s-discoveryengine.googleapis.com:443", location);
    DataStoreServiceSettings settings =
        DataStoreServiceSettings.newBuilder().setEndpoint(endpoint).build();
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `dataStoreServiceClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (DataStoreServiceClient dataStoreServiceClient = DataStoreServiceClient.create(settings)) {
      CollectionName parent = CollectionName.of(projectId, location, "default_collection");
      DataStore dataStore =
          DataStore.newBuilder()
              .setDisplayName("My Data Store")
              .setIndustryVertical(IndustryVertical.GENERIC)
              .addSolutionTypes(SolutionType.SOLUTION_TYPE_SEARCH)
              .setContentConfig(ContentConfig.CONTENT_REQUIRED)
              .build();
      CreateDataStoreRequest request =
          CreateDataStoreRequest.newBuilder()
              .setParent(parent.toString())
              .setDataStoreId(dataStoreId)
              .setDataStore(dataStore)
              .build();
      // Start the long running operation
      OperationFuture<DataStore, CreateDataStoreMetadata> future =
          dataStoreServiceClient.createDataStoreAsync(request);
      System.out.println("Waiting for operation to complete: " + future.getName());
      DataStore response = future.get();
      System.out.println("Created DataStore: " + response.getName());
      System.out.println("DataStore Metadata: " + future.getMetadata().get());
    }
  }
}
// [END genappbuilder_create_data_store]
