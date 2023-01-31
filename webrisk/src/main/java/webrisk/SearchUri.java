/*
 * Copyright 2020 Google LLC
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

// [START webrisk_search_uri]

import com.google.cloud.webrisk.v1.WebRiskServiceClient;
import com.google.webrisk.v1.SearchUrisRequest;
import com.google.webrisk.v1.SearchUrisResponse;
import com.google.webrisk.v1.ThreatType;
import java.io.IOException;

public class SearchUri {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // The URI to be checked for matches.
    String uri = "http://testsafebrowsing.appspot.com/s/malware.html";

    // The ThreatLists to search in. Multiple ThreatLists may be specified.
    ThreatType threatType = ThreatType.MALWARE;

    searchUri(uri, threatType);
  }

  // This method is used to check whether a URI is on a given threatList. Multiple threatLists may
  // be searched in a single query.
  // The response will list all requested threatLists the URI was found to match. If the URI is not
  // found on any of the requested ThreatList an empty response will be returned.
  public static void searchUri(String uri, ThreatType threatType) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `webRiskServiceClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (WebRiskServiceClient webRiskServiceClient = WebRiskServiceClient.create()) {

      SearchUrisRequest searchUrisRequest =
          SearchUrisRequest.newBuilder()
              .addThreatTypes(threatType)
              .setUri(uri)
              .build();

      SearchUrisResponse searchUrisResponse = webRiskServiceClient.searchUris(searchUrisRequest);

      if (!searchUrisResponse.getThreat().getThreatTypesList().isEmpty()) {
        System.out.println("The URL has the following threat: ");
        System.out.println(searchUrisResponse);
      } else {
        System.out.println("The URL is safe!");
      }
    }
  }
}
// [END webrisk_search_uri]
