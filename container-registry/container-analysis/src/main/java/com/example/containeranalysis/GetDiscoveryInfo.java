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

// [START containeranalysis_discovery_info]
import com.google.cloud.devtools.containeranalysis.v1beta1.GrafeasV1Beta1Client;
import com.google.containeranalysis.v1beta1.ProjectName;
import io.grafeas.v1beta1.Occurrence;
import java.io.IOException;
import java.lang.InterruptedException;

public class GetDiscoveryInfo {
  // Retrieves and prints the Discovery Occurrence created for a specified image
  // The Discovery Occurrence contains information about the initial scan on the image
  public static void getDiscoveryInfo(String resourceUrl, String projectId) 
      throws IOException, InterruptedException {
    // String resourceUrl = "https://gcr.io/project/image@sha256:123";
    // String projectId = "my-project-id";
    String filterStr = "kind=\"DISCOVERY\" AND resourceUrl=\"" + resourceUrl + "\"";
    final String projectName = ProjectName.format(projectId);

    // Initialize client that will be used to send requests. After completing all of your requests, 
    // call the "close" method on the client to safely clean up any remaining background resources.
    GrafeasV1Beta1Client client = GrafeasV1Beta1Client.create();
    for (Occurrence o : client.listOccurrences(projectName, filterStr).iterateAll()) {
      System.out.println(o);
    }
  }
}
// [END containeranalysis_discovery_info]
