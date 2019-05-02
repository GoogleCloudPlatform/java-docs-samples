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

// [START containeranalysis_get_note]
import com.google.cloud.devtools.containeranalysis.v1beta1.GrafeasV1Beta1Client;
import com.google.containeranalysis.v1beta1.NoteName;
import io.grafeas.v1beta1.Note;
import java.io.IOException;
import java.lang.InterruptedException;

public class GetNote {  
  /**
   * Retrieves and prints a specified Note from the server
   * @param noteId the Note's unique identifier
   * @param projectId the GCP project the Note belongs to
   * @return the requested Note object
   * @throws IOException on errors creating the Grafeas client
   * @throws InterruptedException on errors shutting down the Grafeas client
   */
  public static Note getNote(String noteId, String projectId) 
      throws IOException, InterruptedException {
    final NoteName noteName = NoteName.of(projectId, noteId);

    // Initialize client that will be used to send requests. After completing all of your requests, 
    // call the "close" method on the client to safely clean up any remaining background resources.
    GrafeasV1Beta1Client client = GrafeasV1Beta1Client.create();
    Note n = client.getNote(noteName);
    System.out.println(n);
    return n;
  }
}
// [END containeranalysis_get_note]
