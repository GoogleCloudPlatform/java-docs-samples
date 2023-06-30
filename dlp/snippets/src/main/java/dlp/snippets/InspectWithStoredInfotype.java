/*
 * Copyright 2023 Google LLC
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

package dlp.snippets;

// [START dlp_inspect_with_stored_infotype]

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.ContentItem;
import com.google.privacy.dlp.v2.CustomInfoType;
import com.google.privacy.dlp.v2.Finding;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.InspectConfig;
import com.google.privacy.dlp.v2.InspectContentRequest;
import com.google.privacy.dlp.v2.InspectContentResponse;
import com.google.privacy.dlp.v2.LocationName;
import com.google.privacy.dlp.v2.ProjectStoredInfoTypeName;
import com.google.privacy.dlp.v2.StoredType;
import java.io.IOException;

public class InspectWithStoredInfotype {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // The Google Cloud project id to use as a parent resource.
    String projectId = "your-project-id";
    // The sample assumes that you have an existing stored infoType.
    // To create a stored InfoType refer:
    // https://cloud.google.com/dlp/docs/creating-stored-infotypes#create-storedinfotye 
    String storedInfoTypeId = "your-info-type-id";
    // The string to de-identify.
    String textToInspect =
        "My phone number is (223) 456-7890 and my email address is gary@example.com.";
    inspectWithStoredInfotype(projectId, storedInfoTypeId, textToInspect);
  }

  //  Inspects the given text using the specified stored infoType detector.
  public static void inspectWithStoredInfotype(
      String projectId, String storedInfoTypeId, String textToInspect) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DlpServiceClient dlp = DlpServiceClient.create()) {

      // Specify the content to be inspected.
      ContentItem contentItem = ContentItem.newBuilder().setValue(textToInspect).build();

      InfoType infoType = InfoType.newBuilder().setName("STORED_TYPE").build();

      // Reference to the existing StoredInfoType to inspect the data.
      StoredType storedType = StoredType.newBuilder()
              .setName(ProjectStoredInfoTypeName.of(projectId, storedInfoTypeId).toString())
              .build();

      CustomInfoType customInfoType =
          CustomInfoType.newBuilder().setInfoType(infoType).setStoredType(storedType).build();

      // Construct the configuration for the Inspect request.
      InspectConfig inspectConfig =
          InspectConfig.newBuilder()
              .addCustomInfoTypes(customInfoType)
              .setIncludeQuote(true)
              .build();

      // Construct the Inspect request to be sent by the client.
      InspectContentRequest inspectContentRequest =
          InspectContentRequest.newBuilder()
              .setParent(LocationName.of(projectId, "global").toString())
              .setInspectConfig(inspectConfig)
              .setItem(contentItem)
              .build();

      // Use the client to send the API request.
      InspectContentResponse response = dlp.inspectContent(inspectContentRequest);

      // Parse the response and process results.
      System.out.println("Findings: " + "" + response.getResult().getFindingsCount());
      for (Finding f : response.getResult().getFindingsList()) {
        System.out.println("\tQuote: " + f.getQuote());
        System.out.println("\tInfoType: " + f.getInfoType().getName());
        System.out.println("\tLikelihood: " + f.getLikelihood() + "\n");
      }
    }
  }
}
// [END dlp_inspect_with_stored_infotype]
