/*
 * Copyright 2023 Google LLC
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
 */

package aiplatform;

// [START aiplatform_read_feature_values_sample]

import com.google.cloud.aiplatform.v1.EntityTypeName;
import com.google.cloud.aiplatform.v1.FeatureSelector;
import com.google.cloud.aiplatform.v1.FeaturestoreOnlineServingServiceClient;
import com.google.cloud.aiplatform.v1.FeaturestoreOnlineServingServiceSettings;
import com.google.cloud.aiplatform.v1.IdMatcher;
import com.google.cloud.aiplatform.v1.ReadFeatureValuesRequest;
import com.google.cloud.aiplatform.v1.ReadFeatureValuesResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class ReadFeatureValuesSample {

  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    String project = "YOUR_PROJECT_ID";
    // Feature Store ID
    String featurestoreId = "YOUR_FEATURESTORE_ID";
    // Entity Type ID
    String entityTypeId = "YOUR_ENTITY_TYPE_ID";
    // Entity ID
    String entityId = "YOUR_ENTITY_ID";
    // Features to read with batch or online serving.
    List<String> featureSelectorIds = Arrays.asList("title", "genres", "average_rating");
    String location = "us-central1";
    String endpoint = "us-central1-aiplatform.googleapis.com:443";
    int timeout = 300;

    readFeatureValuesSample(
        project,
        featurestoreId,
        entityTypeId,
        entityId,
        featureSelectorIds,
        location,
        endpoint,
        timeout);
  }

  /*
   * Reads Feature values of a specific entity of an EntityType.
   * See: https://cloud.google.com/vertex-ai/docs/featurestore/serving-online
   */
  public static void readFeatureValuesSample(
      String project,
      String featurestoreId,
      String entityTypeId,
      String entityId,
      List<String> featureSelectorIds,
      String location,
      String endpoint,
      int timeout)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    FeaturestoreOnlineServingServiceSettings featurestoreOnlineServiceSettings =
        FeaturestoreOnlineServingServiceSettings.newBuilder().setEndpoint(endpoint).build();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (FeaturestoreOnlineServingServiceClient featurestoreOnlineServiceClient =
        FeaturestoreOnlineServingServiceClient.create(featurestoreOnlineServiceSettings)) {
      ReadFeatureValuesRequest readFeatureValuesRequest =
          ReadFeatureValuesRequest.newBuilder()
              .setEntityType(
                  EntityTypeName.of(project, location, featurestoreId, entityTypeId).toString())
              .setEntityId(entityId)
              .setFeatureSelector(
                  FeatureSelector.newBuilder()
                      .setIdMatcher(IdMatcher.newBuilder().addAllIds(featureSelectorIds)))
              .build();

      ReadFeatureValuesResponse readFeatureValuesResponse =
          featurestoreOnlineServiceClient.readFeatureValues(readFeatureValuesRequest);
      System.out.println("Read Feature Values Response" + readFeatureValuesResponse);
    }
  }
}
// [END aiplatform_read_feature_values_sample]
