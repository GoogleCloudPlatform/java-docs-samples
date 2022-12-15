/*
 * Copyright 2022 Google Inc.
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

package webrisk;

// [START webrisk_compute_threatlist_diff]

import com.google.cloud.webrisk.v1.WebRiskServiceClient;
import com.google.protobuf.ByteString;
import com.google.webrisk.v1.CompressionType;
import com.google.webrisk.v1.ComputeThreatListDiffRequest;
import com.google.webrisk.v1.ComputeThreatListDiffRequest.Constraints;
import com.google.webrisk.v1.ComputeThreatListDiffResponse;
import com.google.webrisk.v1.ThreatType;
import java.io.IOException;

public class ComputeThreatListDiff {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // The threat list to update. Only a single ThreatType should be specified per request.
    ThreatType threatType = ThreatType.MALWARE;

    // The current version token of the client for the requested list. If the client does not have
    // a version token (this is the first time calling ComputeThreatListDiff), this may be
    // left empty and a full database snapshot will be returned.
    ByteString versionToken = ByteString.EMPTY;

    // The maximum size in number of entries. The diff will not contain more entries
    // than this value. This should be a power of 2 between 2**10 and 2**20.
    // If zero, no diff size limit is set.
    int maxDiffEntries = 1024;

    // Sets the maximum number of entries that the client is willing to have in the local database.
    // This should be a power of 2 between 2**10 and 2**20. If zero, no database size limit is set.
    int maxDatabaseEntries = 1024;

    // The compression type supported by the client.
    CompressionType compressionType = CompressionType.RAW;

    computeThreatDiffList(threatType, versionToken, maxDiffEntries, maxDatabaseEntries,
        compressionType);
  }

  // Gets the most recent threat list diffs. These diffs should be applied to a local database of
  // hashes to keep it up-to-date.
  // If the local database is empty or excessively out-of-date,
  // a complete snapshot of the database will be returned. This Method only updates a
  // single ThreatList at a time. To update multiple ThreatList databases, this method needs to be
  // called once for each list.
  public static void computeThreatDiffList(ThreatType threatType, ByteString versionToken,
      int maxDiffEntries, int maxDatabaseEntries, CompressionType compressionType)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `webRiskServiceClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (WebRiskServiceClient webRiskServiceClient = WebRiskServiceClient.create()) {

      Constraints constraints = Constraints.newBuilder()
          .setMaxDiffEntries(maxDiffEntries)
          .setMaxDatabaseEntries(maxDatabaseEntries)
          .addSupportedCompressions(compressionType)
          .build();

      ComputeThreatListDiffResponse response = webRiskServiceClient.computeThreatListDiff(
          ComputeThreatListDiffRequest.newBuilder()
              .setThreatType(threatType)
              .setVersionToken(versionToken)
              .setConstraints(constraints)
              .build());

      // The returned response contains the following information:
      // https://cloud.google.com/web-risk/docs/reference/rpc/google.cloud.webrisk.v1#computethreatlistdiffresponse
      // Type of response: DIFF/ RESET/ RESPONSE_TYPE_UNSPECIFIED
      System.out.println(response.getResponseType());
      // List of entries to add and/or remove.
      // System.out.println(response.getAdditions());
      // System.out.println(response.getRemovals());

      // New version token to be used the next time when querying.
      System.out.println(response.getNewVersionToken());

      // Recommended next diff timestamp.
      System.out.println(response.getRecommendedNextDiff());

      System.out.println("Obtained threat list diff.");
    }
  }
}
// [END webrisk_compute_threatlist_diff]