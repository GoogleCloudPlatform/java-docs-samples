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

package aiplatform.vectorsearch;

// [START aiplatform_vector_search_create_streaming_index_sample]

import com.google.cloud.aiplatform.v1.CreateIndexRequest;
import com.google.cloud.aiplatform.v1.Index;
import com.google.cloud.aiplatform.v1.Index.IndexUpdateMethod;
import com.google.cloud.aiplatform.v1.IndexServiceClient;
import com.google.cloud.aiplatform.v1.IndexServiceSettings;
import com.google.cloud.aiplatform.v1.LocationName;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import java.util.concurrent.TimeUnit;

public class CreateStreamingIndexSample {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String project = "YOUR_PROJECT_ID";
    String location = "YOUR_LOCATION";
    String displayName = "YOUR_INDEX_DISPLAY_NAME";
    String contentsDeltaUri = "gs://YOUR_BUCKET/";
    String metadataJson = """
        {
          "contentsDeltaUri": "%s",
          "config": {
            "dimensions": 100,
            "approximateNeighborsCount": 150,
            "distanceMeasureType": "DOT_PRODUCT_DISTANCE",
            "shardSize": "SHARD_SIZE_MEDIUM",
            "algorithm_config": {
              "treeAhConfig": {
                "leafNodeEmbeddingCount": 5000,
                "fractionLeafNodesToSearch": 0.03
              }
            }
          }
        }
        """.formatted(contentsDeltaUri);

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (IndexServiceClient indexServiceClient = IndexServiceClient.create(
        IndexServiceSettings.newBuilder().setEndpoint(location + "-aiplatform.googleapis.com:443")
            .build())) {
      createStreamingIndexSample(project, location, displayName, metadataJson, indexServiceClient);
    }
  }

  /**
   * Creates a streaming index using the provided {@code indexServiceClient} to send the request.
   *
   * @return the created index
   */
  public static Index createStreamingIndexSample(String project, String location, String displayName,
      String metadataJson, IndexServiceClient indexServiceClient) throws Exception {
    Value.Builder metadataBuilder = Value.newBuilder();
    JsonFormat.parser().merge(metadataJson, metadataBuilder);
    CreateIndexRequest request =
        CreateIndexRequest.newBuilder()
            .setParent(LocationName.of(project, location).toString())
            .setIndex(Index.newBuilder()
                .setDisplayName(displayName)
                .setMetadata(
                    metadataBuilder)
                .setIndexUpdateMethod(IndexUpdateMethod.STREAM_UPDATE))
            .build();
    Index response = indexServiceClient.createIndexAsync(request).get(5, TimeUnit.MINUTES);
    return response;
  }
}

// [END aiplatform_vector_search_create_streaming_index_sample]
