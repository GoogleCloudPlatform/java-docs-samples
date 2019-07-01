/*
 * Copyright 2019 Google LLC
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

package com.example.containeranalysis;

// [START containeranalysis_delete_occurrence]
import com.google.cloud.devtools.containeranalysis.v1.ContainerAnalysisClient;
import io.grafeas.v1.GrafeasClient;
import io.grafeas.v1.OccurrenceName;
import java.io.IOException;
import java.lang.InterruptedException;

public class DeleteOccurrence {
  // Deletes an existing Occurrence from the server
  public static void deleteOccurrence(String occurrenceId, String projectId) 
      throws IOException, InterruptedException {
    // String occurrenceId = "123-456-789";
    // String projectId = "my-project-id";
    final OccurrenceName occurrenceName = OccurrenceName.of(projectId, occurrenceId);

    // Initialize client that will be used to send requests. After completing all of your requests, 
    // call the "close" method on the client to safely clean up any remaining background resources.
    GrafeasClient client = ContainerAnalysisClient.create().getGrafeasClient();
    client.deleteOccurrence(occurrenceName);
  }
}
// [END containeranalysis_delete_occurrence]
