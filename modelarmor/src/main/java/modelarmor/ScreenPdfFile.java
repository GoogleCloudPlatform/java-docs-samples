/*
 * Copyright 2025 Google LLC
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

package modelarmor;

import com.google.cloud.modelarmor.v1.ByteDataItem;
import com.google.cloud.modelarmor.v1.ByteDataItem.ByteItemType;
import com.google.cloud.modelarmor.v1.DataItem;
import com.google.cloud.modelarmor.v1.ModelArmorClient;
import com.google.cloud.modelarmor.v1.ModelArmorSettings;
import com.google.cloud.modelarmor.v1.SanitizeUserPromptRequest;
import com.google.cloud.modelarmor.v1.SanitizeUserPromptResponse;
import com.google.cloud.modelarmor.v1.TemplateName;
import com.google.protobuf.ByteString;
import com.google.protobuf.util.JsonFormat;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ScreenPdfFile {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";
    String templateId = "your-template-id";
    String pdfFilePath = "src/main/resources/ma-prompt.pdf"; // Replace with your PDF file path

    screenPdfFile(projectId, locationId, templateId, pdfFilePath);
  }

  public static void screenPdfFile(
      String projectId, String locationId, String templateId, String pdfFilePath) throws Exception {
    // Endpoint to call the Model Armor server.
    String apiEndpoint = String.format("modelarmor.%s.rep.googleapis.com:443", locationId);
    ModelArmorSettings modelArmorSettings =
        ModelArmorSettings.newBuilder().setEndpoint(apiEndpoint).build();

    try (ModelArmorClient client = ModelArmorClient.create(modelArmorSettings)) {
      String name = TemplateName.of(projectId, locationId, templateId).toString();

      // Read the PDF file content and encode it to Base64.
      byte[] fileContent = Files.readAllBytes(Paths.get(pdfFilePath));

      // Build the request.
      DataItem userPromptData =
          DataItem.newBuilder()
              .setByteItem(
                  ByteDataItem.newBuilder()
                      .setByteDataType(ByteItemType.PDF)
                      .setByteData(ByteString.copyFrom(fileContent))
                      .build())
              .build();

      SanitizeUserPromptRequest request =
          SanitizeUserPromptRequest.newBuilder()
              .setName(name)
              .setUserPromptData(userPromptData)
              .build();

      // Send the request and get the response.
      SanitizeUserPromptResponse response = client.sanitizeUserPrompt(request);

      // Print the sanitization result.
      System.out.println(
          "Sanitized PDF File: " + JsonFormat.printer().print(response.getSanitizationResult()));
    }
  }
}
