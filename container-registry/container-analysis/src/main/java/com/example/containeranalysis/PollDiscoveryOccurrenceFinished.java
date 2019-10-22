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

// [START containeranalysis_poll_discovery_occurrence_finished]
import com.google.cloud.devtools.containeranalysis.v1.ContainerAnalysisClient;
import io.grafeas.v1.DiscoveryOccurrence;
import io.grafeas.v1.DiscoveryOccurrence.AnalysisStatus;
import io.grafeas.v1.GrafeasClient;
import io.grafeas.v1.Occurrence;
import io.grafeas.v1.ProjectName;
import java.io.IOException;
import java.lang.InterruptedException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PollDiscoveryOccurrenceFinished {
  // Repeatedly query the Container Analysis API for the latest discovery occurrence until it is
  // either in a terminal state, or the timeout value has been exceeded
  public static Occurrence pollDiscoveryOccurrenceFinished(String resourceUrl, String projectId,
      long timeoutSeconds) throws IOException, TimeoutException, InterruptedException {
    // String resourceUrl = "https://gcr.io/project/image@sha256:123";
    // String projectId = "my-project-id";
    // long timeoutSeconds = 30;
    final String projectName = ProjectName.format(projectId);
    long deadline = System.currentTimeMillis() + timeoutSeconds * 1000;

    // Initialize client that will be used to send requests. After completing all of your requests, 
    // call the "close" method on the client to safely clean up any remaining background resources.
    GrafeasClient client = ContainerAnalysisClient.create().getGrafeasClient();

    // find the discovery occurrence using a filter string
    Occurrence discoveryOccurrence = null;
    // vulbnerability discovery occurrences are always associated with the
    // PACKAGE_VULNERABILITY note in the "goog-analysis" GCP project
    String filter =  String.format("resourceUrl=\"%s\" AND noteProjectId=\"%s\" AND noteId=\"%s\"", 
        resourceUrl, "goog-analysis",  "PACKAGE_VULNERABILITY");
    // [END containeranalysis_poll_discovery_occurrence_finished]
    // the above filter isn't testable, since it looks for occurrences in a locked down project
    // fall back to a more permissive filter for testing
    filter = String.format("kind=\"DISCOVERY\" AND resourceUrl=\"%s\"", resourceUrl);
    // [START containeranalysis_poll_discovery_occurrence_finished]
    while (discoveryOccurrence == null) {
      for (Occurrence o : client.listOccurrences(projectName, filter).iterateAll()) {
        if (o.getDiscovery() != null) {
          // there should be only one valid discovery occurrence returned by the given filter
          discoveryOccurrence = o;
        }
      }
      TimeUnit.SECONDS.sleep(1);
      // check for timeout
      if (System.currentTimeMillis() > deadline) {
        throw new TimeoutException("discovery occurrence not found");
      }
    }

    // wait for discovery occurrence to enter a terminal state
    AnalysisStatus status = AnalysisStatus.PENDING;
    while (status != AnalysisStatus.FINISHED_SUCCESS
        && status != AnalysisStatus.FINISHED_FAILED
        && status != AnalysisStatus.FINISHED_UNSUPPORTED) {
      // update the occurrence state
      discoveryOccurrence = client.getOccurrence(discoveryOccurrence.getName());
      status = discoveryOccurrence.getDiscovery().getAnalysisStatus();
      TimeUnit.SECONDS.sleep(1);
      // check for timeout
      if (System.currentTimeMillis() > deadline) {
        throw new TimeoutException("discovery occurrence not in terminal state");
      }
    }
    return discoveryOccurrence;
  }
}
// [END containeranalysis_poll_discovery_occurrence_finished]
