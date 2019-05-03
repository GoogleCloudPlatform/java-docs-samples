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
import com.google.cloud.devtools.containeranalysis.v1beta1.GrafeasV1Beta1Client;
import com.google.containeranalysis.v1beta1.ProjectName;
import io.grafeas.v1beta1.Note;
import io.grafeas.v1beta1.vulnerability.Severity;
import io.grafeas.v1beta1.vulnerability.Vulnerability;
import io.grafeas.v1beta1.vulnerability.Vulnerability.Detail;
import java.io.IOException;
import java.lang.InterruptedException;


public class CreateNote {

  // Creates and returns a new Note
  public static Note createNote(String noteId, String projectId)
      throws IOException, InterruptedException {
    // String noteId = "my-note";
    // String projectId = "my-project-id";
    final String projectName = ProjectName.format(projectId);

    Note.Builder noteBuilder = Note.newBuilder();
    // Associate the Note with the metadata type
    // https://cloud.google.com/container-registry/docs/container-analysis#supported_metadata_types
    // Here, we use the type "vulnerability"
    Vulnerability.Builder vulBuilder = Vulnerability.newBuilder();
    noteBuilder.setVulnerability(vulBuilder);
    // Set additional information specific to your new vulnerability note
    Detail.Builder detailsBuilder = Detail.newBuilder();
    detailsBuilder.setDescription("my new vulnerability note");
    vulBuilder.setSeverity(Severity.LOW);
    vulBuilder.addDetails(detailsBuilder);
    // Build the Note object
    Note newNote = noteBuilder.build();

    // Initialize client that will be used to send requests. After completing all of your requests, 
    // call the "close" method on the client to safely clean up any remaining background resources.
    GrafeasV1Beta1Client client = GrafeasV1Beta1Client.create();
    Note result = client.createNote(projectName, noteId, newNote);
    return result;
  }
}
// [END containeranalysis_create_note]
