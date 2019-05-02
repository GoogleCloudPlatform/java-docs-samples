/*
 * Copyright 2018 Google LLC
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

// [START containeranalysis_poll_discovery_occurrence_finished]i
import static java.lang.Thread.sleep;

import com.google.cloud.devtools.containeranalysis.v1beta1.GrafeasV1Beta1Client;
import com.google.containeranalysis.v1beta1.ProjectName;
import io.grafeas.v1beta1.Occurrence;
import io.grafeas.v1beta1.discovery.Discovered.AnalysisStatus;
import java.io.IOException;
import java.lang.InterruptedException;
import java.util.concurrent.TimeoutException;

public class PollDiscoveryOccurrenceFinished {
  // Repeatedly query the Container Analysis API for the latest discovery occurrence until it is
  // either in a terminal state, or the timeout value has been exceeded
  public static Occurrence pollDiscoveryOccurrenceFinished(String resourceUrl, String projectId,
      long timeoutSeconds) throws IOException, TimeoutException, InterruptedException {
    // String resourceUrl = "https://gcr.io/project/image@sha256:foo";
    // String projectId = "my-project-id";
    // long timeoutSeconds = 30;
    final String projectName = ProjectName.format(projectId);
    long deadline = System.currentTimeMillis() + timeoutSeconds * 1000;

    // Initialize client that will be used to send requests. After completing all of your requests, 
    // call the "close" method on the client to safely clean up any remaining background resources.
    GrafeasV1Beta1Client client = GrafeasV1Beta1Client.create();

    // find the discovery occurrence using a filter string
    Occurrence discoveryOccurrence = null;
    String filterStr = "kind=\"DISCOVERY\" AND resourceUrl=\"" + resourceUrl + "\"";
    while (discoveryOccurrence == null) {
      for (Occurrence o : client.listOccurrences(projectName, filterStr).iterateAll()) {
        if (o.getDiscovered() != null) {
          // there should be only one valid discovery occurrence returned by the given filter
          discoveryOccurrence = o;
        }
      }
      sleep(1);
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
      status = discoveryOccurrence.getDiscovered().getDiscovered().getAnalysisStatus();
      sleep(1);
      // check for timeout
      if (System.currentTimeMillis() > deadline) {
        throw new TimeoutException("discovery occurrence not in terminal state");
      }
    }
    return discoveryOccurrence;
  }
}
// [END containeranalysis_poll_discovery_occurrence_finished]
