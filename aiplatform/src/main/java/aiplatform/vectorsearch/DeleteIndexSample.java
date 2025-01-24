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

// [START aiplatform_vector_search_delete_index_sample]

import com.google.cloud.aiplatform.v1.IndexName;
import com.google.cloud.aiplatform.v1.IndexServiceClient;
import com.google.cloud.aiplatform.v1.IndexServiceSettings;
import java.util.concurrent.TimeUnit;

public class DeleteIndexSample {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String project = "YOUR_PROJECT_ID";
    String location = "YOUR_LOCATION";
    String indexId = "YOUR_INDEX_ID";

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (IndexServiceClient indexServiceClient = IndexServiceClient.create(
        IndexServiceSettings.newBuilder().setEndpoint(location + "-aiplatform.googleapis.com:443")
            .build())) {
      deleteIndexSample(project, location, indexId, indexServiceClient);
    }
  }

  /**
   * Deletes an index using the provided {@code indexServiceClient} to send the request.
   */
  public static void deleteIndexSample(String project, String location, String indexId,
      IndexServiceClient indexServiceClient) throws Exception {
    String indexName = IndexName.of(project, location, indexId).toString();
    indexServiceClient.deleteIndexAsync(indexName).get(5, TimeUnit.MINUTES);
  }
}

// [END aiplatform_vector_search_delete_index_sample]
