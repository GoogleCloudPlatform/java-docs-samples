/*
 * Copyright 2020 Google LLC
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

// [START asset_quickstart_search_all_iam_policies]
import com.google.api.gax.rpc.ApiException;
import com.google.api.gax.rpc.InvalidArgumentException;
import com.google.cloud.asset.v1.AssetServiceClient;
import com.google.cloud.asset.v1.AssetServiceClient.SearchAllIamPoliciesPagedResponse;
import com.google.cloud.asset.v1.SearchAllIamPoliciesRequest;
import java.io.IOException;

public class SearchAllIamPoliciesExample {

  public static void searchAllIamPolicies(
      String scope, String query, int pageSize, String pageToken) {
    SearchAllIamPoliciesRequest request =
        SearchAllIamPoliciesRequest.newBuilder()
            .setScope(scope)
            .setQuery(query)
            .setPageSize(pageSize)
            .setPageToken(pageToken)
            .build();
    try (AssetServiceClient client = AssetServiceClient.create()) {
      SearchAllIamPoliciesPagedResponse response = client.searchAllIamPolicies(request);
      System.out.println("Search completed successfully:\n" + response.getPage().getValues());
    } catch (IOException e) {
      System.out.println("Failed to create client:\n" + e.toString());
    } catch (InvalidArgumentException e) {
      System.out.println("Invalid request:\n" + e.toString());
    } catch (ApiException e) {
      System.out.println("Error during SearchAllIamPolicies:\n" + e.toString());
    }
  }
}
// [END asset_quickstart_search_all_iam_policies]
