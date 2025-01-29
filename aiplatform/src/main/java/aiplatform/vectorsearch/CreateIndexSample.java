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

// [START aiplatform_sdk_vector_search_create_index_sample]

import com.google.cloud.aiplatform.v1.CreateIndexRequest;
import com.google.cloud.aiplatform.v1.Index;
import com.google.cloud.aiplatform.v1.Index.IndexUpdateMethod;
import com.google.cloud.aiplatform.v1.IndexServiceClient;
import com.google.cloud.aiplatform.v1.IndexServiceSettings;
import com.google.cloud.aiplatform.v1.LocationName;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import java.util.concurrent.TimeUnit;

public class CreateIndexSample {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String project = "YOUR_PROJECT_ID";
    String location = "YOUR_LOCATION";
    String displayName = "YOUR_INDEX_DISPLAY_NAME";
    String contentsDeltaUri = "gs://YOUR_BUCKET/";
    String metadataJson =
        String.format(
            "{\n"
                + "  \"contentsDeltaUri\": \"%s\",\n"
                + "  \"config\": {\n"
                + "    \"dimensions\": 100,\n"
                + "        \"approximateNeighborsCount\": 150,\n"
                + "        \"distanceMeasureType\": \"DOT_PRODUCT_DISTANCE\",\n"
                + "        \"shardSize\": \"SHARD_SIZE_MEDIUM\",\n"
                + "        \"algorithm_config\": {\n"
                + "      \"treeAhConfig\": {\n"
                + "        \"leafNodeEmbeddingCount\": 5000,\n"
                + "            \"fractionLeafNodesToSearch\": 0.03\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "}",
            contentsDeltaUri);

    createIndexSample(project, location, displayName, metadataJson);
  }

  public static Index createIndexSample(
      String project, String location, String displayName, String metadataJson) throws Exception {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (IndexServiceClient indexServiceClient =
        IndexServiceClient.create(
            IndexServiceSettings.newBuilder()
                .setEndpoint(location + "-aiplatform.googleapis.com:443")
                .build())) {
      Value.Builder metadataBuilder = Value.newBuilder();
      JsonFormat.parser().merge(metadataJson, metadataBuilder);

      CreateIndexRequest request =
          CreateIndexRequest.newBuilder()
              .setParent(LocationName.of(project, location).toString())
              .setIndex(
                  Index.newBuilder()
                      .setDisplayName(displayName)
                      .setMetadata(metadataBuilder)
                      .setIndexUpdateMethod(IndexUpdateMethod.BATCH_UPDATE))
              .build();

      return indexServiceClient.createIndexAsync(request).get(5, TimeUnit.MINUTES);
    }
  }
}

// [END aiplatform_sdk_vector_search_create_index_sample]
