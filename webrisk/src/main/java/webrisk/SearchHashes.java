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

import com.google.cloud.webrisk.v1.WebRiskServiceClient;
import com.google.protobuf.ByteString;
import com.google.webrisk.v1.SearchHashesRequest;
import com.google.webrisk.v1.SearchHashesResponse;
import com.google.webrisk.v1.SearchHashesResponse.ThreatHash;
import com.google.webrisk.v1.ThreatType;
import java.io.IOException;

public class SearchHashes {

  public static void main(String[] args) throws IOException {
    searchHash();
  }

  public static void searchHash() throws IOException {
    try (WebRiskServiceClient webRiskServiceClient = WebRiskServiceClient.create()) {

      SearchHashesResponse response = webRiskServiceClient.searchHashes(
          SearchHashesRequest.newBuilder()
              .setHashPrefix(ByteString.fromHex("0AEF23DE"))
              .addThreatTypes(ThreatType.MALWARE)
              .build());

      for (ThreatHash threatHash : response.getThreatsList()) {
        System.out.println(threatHash.getHash());
      }
      System.out.println("Completed listing threat hashes.");
    }
  }
}