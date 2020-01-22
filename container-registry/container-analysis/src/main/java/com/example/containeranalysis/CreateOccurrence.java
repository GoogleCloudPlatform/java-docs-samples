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

// [START containeranalysis_create_occurrence]
import com.google.cloud.devtools.containeranalysis.v1.ContainerAnalysisClient;
import io.grafeas.v1.GrafeasClient;
import io.grafeas.v1.NoteName;
import io.grafeas.v1.Occurrence;
import io.grafeas.v1.ProjectName;
import io.grafeas.v1.Version;
import io.grafeas.v1.VulnerabilityOccurrence;
import io.grafeas.v1.VulnerabilityOccurrence.PackageIssue;
import java.io.IOException;
import java.lang.InterruptedException;

public class CreateOccurrence {
  // Creates and returns a new vulnerability Occurrence associated with an existing Note
  public static Occurrence createOccurrence(String resourceUrl, String noteId, 
      String occProjectId, String noteProjectId) throws IOException, InterruptedException {
    // String resourceUrl = "https://gcr.io/project/image@sha256:123";
    // String noteId = "my-note";
    // String occProjectId = "my-project-id";
    // String noteProjectId = "my-project-id";
    final NoteName noteName = NoteName.of(noteProjectId, noteId);
    final String occProjectName = ProjectName.format(occProjectId);

    Occurrence newOcc = Occurrence.newBuilder()
        .setNoteName(noteName.toString())
        .setResourceUri(resourceUrl)
        .setVulnerability(VulnerabilityOccurrence.newBuilder()
            .addPackageIssue(PackageIssue.newBuilder()
                .setAffectedCpeUri("your-uri-here")
                .setAffectedPackage("your-package-here")
                .setAffectedVersion(Version.newBuilder()
                    .setKind(Version.VersionKind.MINIMUM))
                .setFixedVersion(Version.newBuilder()
                    .setKind(Version.VersionKind.MAXIMUM))))
        .build();

    // Initialize client that will be used to send requests. After completing all of your requests, 
    // call the "close" method on the client to safely clean up any remaining background resources.
    GrafeasClient client = ContainerAnalysisClient.create().getGrafeasClient();
    Occurrence result = client.createOccurrence(occProjectName, newOcc);
    return result;
  }
}
// [END containeranalysis_create_occurrence]
