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

package aiplatform.vectorsearch;

// [START aiplatform_sdk_vector_search_list_index_sample]

import com.google.cloud.aiplatform.v1.Index;
import com.google.cloud.aiplatform.v1.IndexServiceClient;
import com.google.cloud.aiplatform.v1.IndexServiceClient.ListIndexesPagedResponse;
import com.google.cloud.aiplatform.v1.IndexServiceSettings;
import com.google.cloud.aiplatform.v1.LocationName;

public class ListIndexesSample {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String project = "YOUR_PROJECT_ID";
    String location = "YOUR_LOCATION";

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (IndexServiceClient indexServiceClient = IndexServiceClient.create(
        IndexServiceSettings.newBuilder().setEndpoint(location + "-aiplatform.googleapis.com:443")
            .build())) {
      for (Index index : listIndexesSample(project, location, indexServiceClient).iterateAll()) {
        System.out.println(index);
      }
    }
  }


  /**
   * Lists existing indexes using {@code indexServiceClient} to send the request.
   *
   * @returns the list of indexes
   */
  public static ListIndexesPagedResponse listIndexesSample(String project, String location,
      IndexServiceClient indexServiceClient) throws Exception {
    String parent = LocationName.of(project, location).toString();
    return indexServiceClient.listIndexes(parent);
  }
}

// [END aiplatform_sdk_vector_search_list_index_sample]
