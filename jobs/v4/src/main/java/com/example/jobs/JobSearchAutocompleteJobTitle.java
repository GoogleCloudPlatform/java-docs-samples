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

package com.example.jobs;

import com.google.cloud.talent.v4beta1.CompleteQueryRequest;
import com.google.cloud.talent.v4beta1.CompleteQueryResponse;
import com.google.cloud.talent.v4beta1.CompletionClient;
import com.google.cloud.talent.v4beta1.TenantName;
import com.google.cloud.talent.v4beta1.TenantOrProjectName;

import java.util.Arrays;
import java.util.List;

public class JobSearchAutocompleteJobTitle {

  // [START job_search_autocomplete_job_title]

  /**
   * Complete job title given partial text (autocomplete)
   *
   * @param projectId Your Google Cloud Project ID
   * @param tenantId Identifier of the Tenantd
   */
  public static void completeQuery(
      String projectId, String tenantId, String query, int numResults, String languageCode) {
    // [START job_search_autocomplete_job_title_core]
    try (CompletionClient completionClient = CompletionClient.create()) {
      // projectId = "Your Google Cloud Project ID";
      // tenantId = "Your Tenant ID (using tenancy is optional)";
      // query = "[partially typed job title]";
      // numResults = 5;
      // languageCode = "en-US";
      TenantOrProjectName parent = TenantName.of(projectId, tenantId);
      List<String> languageCodes = Arrays.asList(languageCode);
      CompleteQueryRequest request =
          CompleteQueryRequest.newBuilder()
              .setParent(parent.toString())
              .setQuery(query)
              .setPageSize(numResults)
              .addAllLanguageCodes(languageCodes)
              .build();
      CompleteQueryResponse response = completionClient.completeQuery(request);
      for (CompleteQueryResponse.CompletionResult result : response.getCompletionResultsList()) {
        System.out.printf("Suggested title: %s\n", result.getSuggestion());
        // Suggestion type is JOB_TITLE or COMPANY_TITLE
        System.out.printf("Suggestion type: %s\n", result.getType());
      }
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
    // [END job_search_autocomplete_job_title_core]
  }
  // [END job_search_autocomplete_job_title]

}
