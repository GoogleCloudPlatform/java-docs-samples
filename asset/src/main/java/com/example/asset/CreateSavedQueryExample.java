/*
 * Copyright 2022 Google LLC
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

package com.example.asset;

// [START asset_quickstart_create_saved_query]
import com.google.cloud.asset.v1.AssetServiceClient;
import com.google.cloud.asset.v1.CreateSavedQueryRequest;
import com.google.cloud.asset.v1.IamPolicyAnalysisQuery;
import com.google.cloud.asset.v1.IamPolicyAnalysisQuery.AccessSelector;
import com.google.cloud.asset.v1.ProjectName;
import com.google.cloud.asset.v1.SavedQuery;
import java.io.IOException;
import java.util.Arrays;

public class CreateSavedQueryExample {
  public static void main(String[] args) throws IOException, IllegalArgumentException {
    // TODO(developer): Replace these variables before running the sample.
    String savedQueryId = "MY_SAVED_QUERY_ID";
    String description = "SOME_DESCRIPTION";
    String projectId = "MY_PROJECT_ID";
    createSavedQuery(savedQueryId, description, projectId);
  }

  // Create a savedQuery
  public static void createSavedQuery(
      String savedQueryId, String description, String projectId)
      throws IOException, IllegalArgumentException {
    String parent = String.format(ProjectName.of(projectId).toString());
    SavedQuery savedQuery =
        SavedQuery.newBuilder()
            .setDescription(description)
            .setContent(
                SavedQuery.QueryContent.newBuilder()
                    .setIamPolicyAnalysisQuery(
                        IamPolicyAnalysisQuery.newBuilder()
                          .setScope(parent)
                          .setAccessSelector(
                              AccessSelector.newBuilder()
                                .addPermissions("iam.serviceAccounts.actAs")
                                .build())
                          .build()
                        )
                    .build())
            .build();
    CreateSavedQueryRequest request =
        CreateSavedQueryRequest.newBuilder()
            .setParent(parent)
            .setSavedQueryId(savedQueryId)
            .setSavedQuery(savedQuery)
            .build();
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    AssetServiceClient client = AssetServiceClient.create();
    SavedQuery response = client.createSavedQuery(request);
    System.out.println("SavedQuery created successfully: " + response.getName());
  }
}
// [END asset_quickstart_create_saved_query]
