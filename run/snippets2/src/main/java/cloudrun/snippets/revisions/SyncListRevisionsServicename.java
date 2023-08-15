/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloudrun.snippets.revisions;

// [START cloudrun_revisionsclient_listrevisions_servicename_sync]
import java.io.IOException;
import com.google.cloud.run.v2.Revision;
import com.google.cloud.run.v2.RevisionsClient;
import com.google.cloud.run.v2.ServiceName;

public class SyncListRevisionsServicename {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "PROJECT";
    String location = "us-central1";
    syncListRevisionsServicename(projectId, location);
  }

  public static void syncListRevisionsServicename(String projectId, String location) throws IOException {

    try (RevisionsClient revisionsClient = RevisionsClient.create()) {
      ServiceName parent = ServiceName.of(projectId, location, "[SERVICE]");
      for (Revision element : revisionsClient.listRevisions(parent).iterateAll()) {
        // doThingsWith(element);
      }
    }
  }
}
// [END cloudrun_revisionsclient_listrevisions_servicename_sync]
