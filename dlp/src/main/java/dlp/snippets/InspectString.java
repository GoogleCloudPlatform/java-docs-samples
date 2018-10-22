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

package dlp.snippets;

// [START dlp_inspect_string]
import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.ByteContentItem;
import com.google.privacy.dlp.v2.ByteContentItem.BytesType;
import com.google.privacy.dlp.v2.ContentItem;
import com.google.privacy.dlp.v2.Finding;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.InspectConfig;
import com.google.privacy.dlp.v2.InspectContentRequest;
import com.google.privacy.dlp.v2.InspectContentResponse;
import com.google.privacy.dlp.v2.ProjectName;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;

public class InspectString {

  // Inspects the provided text.
  public static void inspectString(String projectId, String textToInspect) {
    // String projectId = "my-project-id";
    // String textToInspect = "My name is Gary and my email is gary@example.com";

    // Initialize client with try-with-resources for automatic cleanup of background resources
    try (DlpServiceClient dlp = DlpServiceClient.create()) {
      // Set project for request
      ProjectName project = ProjectName.of(projectId);

      // Set content for request
      ByteContentItem byteItem = ByteContentItem.newBuilder()
          .setType(BytesType.TEXT_UTF8)
          .setData(ByteString.copyFromUtf8(textToInspect))
          .build();
      ContentItem item = ContentItem.newBuilder().setByteItem(byteItem).build();

      // Set required InfoTypes for inspection config
      List<InfoType> infoTypes = new ArrayList<>();
      // See https://cloud.google.com/dlp/docs/infotypes-reference for complete list of info types
      for (String typeName : new String[] {"PHONE_NUMBER", "EMAIL_ADDRESS", "CREDIT_CARD_NUMBER"}) {
        infoTypes.add(InfoType.newBuilder().setName(typeName).build());
      }

      // Set the inspect configuration for request
      InspectConfig config = InspectConfig.newBuilder()
          .addAllInfoTypes(infoTypes)
          .setIncludeQuote(true)
          .build();

      // Construct request
      InspectContentRequest request = InspectContentRequest.newBuilder()
          .setParent(project.toString())
          .setItem(item)
          .setInspectConfig(config)
          .build();

      // Run request and parse response
      InspectContentResponse response = dlp.inspectContent(request);
      System.out.println("Findings: " + response.getResult().getFindingsCount());
      for (Finding f : response.getResult().getFindingsList()) {
        System.out.println("\tQuote: " + f.getQuote());
        System.out.println("\tInfo type: " + f.getInfoType().getName());
        System.out.println("\tLikelihood: " + f.getLikelihood());
      }
    } catch (Exception e) {
      System.out.println("Error during inspectString: \n" + e.toString());
    }
  }
}
// [END dlp_inspect_string]
