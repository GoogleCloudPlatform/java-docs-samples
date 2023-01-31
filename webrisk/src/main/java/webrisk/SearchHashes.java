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

// [START webrisk_search_hash]

import com.google.cloud.webrisk.v1.WebRiskServiceClient;
import com.google.protobuf.ByteString;
import com.google.webrisk.v1.SearchHashesRequest;
import com.google.webrisk.v1.SearchHashesResponse;
import com.google.webrisk.v1.SearchHashesResponse.ThreatHash;
import com.google.webrisk.v1.ThreatType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class SearchHashes {

  public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
    // TODO(developer): Replace these variables before running the sample.
    // A hash prefix, consisting of the most significant 4-32 bytes of a SHA256 hash.
    // For JSON requests, this field is base64-encoded. Note that if this parameter is provided
    // by a URI, it must be encoded using the web safe base64 variant (RFC 4648).
    String uri = "http://example.com";
    String encodedUri = Base64.getUrlEncoder().encodeToString(uri.getBytes(StandardCharsets.UTF_8));
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] encodedHashPrefix = digest.digest(encodedUri.getBytes(StandardCharsets.UTF_8));

    // The ThreatLists to search in. Multiple ThreatLists may be specified.
    // For the list on threat types, see: https://cloud.google.com/web-risk/docs/reference/rpc/google.cloud.webrisk.v1#threattype
    List<ThreatType> threatTypes = Arrays.asList(ThreatType.MALWARE, ThreatType.SOCIAL_ENGINEERING);

    searchHash(ByteString.copyFrom(encodedHashPrefix), threatTypes);
  }

  // Gets the full hashes that match the requested hash prefix.
  // This is used after a hash prefix is looked up in a threatList and there is a match.
  // The client side threatList only holds partial hashes so the client must query this method
  // to determine if there is a full hash match of a threat.
  public static void searchHash(ByteString encodedHashPrefix, List<ThreatType> threatTypes)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `webRiskServiceClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (WebRiskServiceClient webRiskServiceClient = WebRiskServiceClient.create()) {

      // Set the hashPrefix and the threat types to search in.
      SearchHashesResponse response = webRiskServiceClient.searchHashes(
          SearchHashesRequest.newBuilder()
              .setHashPrefix(encodedHashPrefix)
              .addAllThreatTypes(threatTypes)
              .build());

      // Get all the hashes that match the prefix. Cache the returned hashes until the time
      // specified in threatHash.getExpireTime()
      // For more information on response type, see: https://cloud.google.com/web-risk/docs/reference/rpc/google.cloud.webrisk.v1#threathash
      for (ThreatHash threatHash : response.getThreatsList()) {
        System.out.println(threatHash.getHash());
      }
      System.out.println("Completed searching threat hashes.");
    }
  }
}
// [END webrisk_search_hash]