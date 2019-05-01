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

// [START containeranalysis_occurrences_for_image]
import com.google.cloud.devtools.containeranalysis.v1beta1.GrafeasV1Beta1Client;
import com.google.containeranalysis.v1beta1.ProjectName;
import io.grafeas.v1beta1.Occurrence;
import java.io.IOException;
import java.lang.InterruptedException;

public class OccurrencesForImage {
  /**
   * Retrieves all the Occurrences associated with a specified image
   * Here, all Occurrences are simply printed and counted
   * @param resourceUrl the Container Registry URL associated with the image
   *                 example: "https://gcr.io/project/image@sha256:foo"
   * @param projectId the GCP project to search for Occurrences in
   * @return number of Occurrences found
   * @throws IOException on errors creating the Grafeas client
   * @throws InterruptedException on errors shutting down the Grafeas client
   */
  public static int getOccurrencesForImage(String resourceUrl, String projectId)
      throws IOException, InterruptedException {
    final String filterStr = "resourceUrl=\"" + resourceUrl + "\"";
    final String projectName = ProjectName.format(projectId);
    int i = 0;

    GrafeasV1Beta1Client client = GrafeasV1Beta1Client.create();
    for (Occurrence o : client.listOccurrences(projectName, filterStr).iterateAll()) {
      // Write custom code to process each Occurrence here
      System.out.println(o.getName());
      i = i + 1;
    }
    return i;
  }
}
// [END containeranalysis_occurrences_for_image]
