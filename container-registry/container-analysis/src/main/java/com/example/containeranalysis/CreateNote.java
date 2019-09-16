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

// [START containeranalysis_create_note]
import com.google.cloud.devtools.containeranalysis.v1.ContainerAnalysisClient;
import io.grafeas.v1.GrafeasClient;
import io.grafeas.v1.Note;
import io.grafeas.v1.ProjectName;
import io.grafeas.v1.Version;
import io.grafeas.v1.VulnerabilityNote;
import java.io.IOException;
import java.lang.InterruptedException;


public class CreateNote {

  // Creates and returns a new Note
  public static Note createNote(String noteId, String projectId)
      throws IOException, InterruptedException {
    // String noteId = "my-note";
    // String projectId = "my-project-id";
    final String projectName = ProjectName.format(projectId);


    Note newNote = Note.newBuilder()
        // Associate the Note with the metadata type
        // https://cloud.google.com/container-registry/docs/container-analysis#supported_metadata_types
        // Here, we use the type "vulnerability"
        .setVulnerability(VulnerabilityNote.newBuilder()
            .addDetails(VulnerabilityNote.Detail.newBuilder()
                .setAffectedCpeUri("your-uri-here")
                .setAffectedPackage("your-package-here")
                .setAffectedVersionStart(Version.newBuilder()
                    .setKind(Version.VersionKind.MINIMUM))
                .setAffectedVersionEnd(Version.newBuilder()
                    .setKind(Version.VersionKind.MAXIMUM))))
        .build();

    // Initialize client that will be used to send requests. After completing all of your requests, 
    // call the "close" method on the client to safely clean up any remaining background resources.
    GrafeasClient client = ContainerAnalysisClient.create().getGrafeasClient();
    Note result = client.createNote(projectName, noteId, newNote);
    return result;
  }
}
// [END containeranalysis_create_note]
