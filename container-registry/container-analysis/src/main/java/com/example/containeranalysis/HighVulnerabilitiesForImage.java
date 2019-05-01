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

// [START containeranalysis_filter_vulnerability_occurrences]
import com.google.cloud.devtools.containeranalysis.v1beta1.GrafeasV1Beta1Client;
import com.google.containeranalysis.v1beta1.ProjectName;
import io.grafeas.v1beta1.Occurrence;
import io.grafeas.v1beta1.vulnerability.Severity;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class HighVulnerabilitiesForImage {
  public static List<Occurrence> findHighSeverityVulnerabilitiesForImage(String resourceUrl,
      String projectId) throws IOException {
    final String projectName = ProjectName.format(projectId);
    GrafeasV1Beta1Client client = GrafeasV1Beta1Client.create();
    String filterStr = "kind=\"VULNERABILITY\" AND resourceUrl=\"" + resourceUrl + "\"";

    LinkedList<Occurrence> vulnerabilitylist = new LinkedList<Occurrence>();
    for (Occurrence o : client.listOccurrences(projectName, filterStr).iterateAll()) {
      Severity severity = o.getVulnerability().getSeverity();
      if (severity == Severity.HIGH || severity == Severity.CRITICAL) {
        vulnerabilitylist.add(o);
      }
    }
    return vulnerabilitylist;
  }
}
// [END containeranalysis_filter_vulnerability_occurrences]
