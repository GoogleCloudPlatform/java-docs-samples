/*
 * Copyright 2024 Google LLC
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

package vertexai.gemini;

// [START generativeaionvertexai_grounding_private_data_basic]
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GroundingMetadata;
import com.google.cloud.vertexai.api.Retrieval;
import com.google.cloud.vertexai.api.Tool;
import com.google.cloud.vertexai.api.VertexAISearch;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import java.io.IOException;
import java.util.Collections;

public class GroundingWithPrivateData {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-google-cloud-project-id";
    String location = "us-central1";
    String modelName = "gemini-1.5-flash-001";
    String datastore = String.format(
        "projects/%s/locations/global/collections/default_collection/dataStores/%s",
        projectId, "datastore_id");

    groundWithPrivateData(projectId, location, modelName, datastore);
  }

  // A request whose response will be "grounded"
  // with information found in Vertex AI Search datastores.
  public static String groundWithPrivateData(String projectId, String location, String modelName,
                                             String datastoreId)
      throws IOException {
    // Initialize client that will be used to send requests.
    // This client only needs to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      Tool datastoreTool = Tool.newBuilder()
          .setRetrieval(
              Retrieval.newBuilder()
                  .setVertexAiSearch(VertexAISearch.newBuilder().setDatastore(datastoreId))
                  .setDisableAttribution(false))
          .build();

      GenerativeModel model = new GenerativeModel(modelName, vertexAI).withTools(
          Collections.singletonList(datastoreTool)
      );

      GenerateContentResponse response = model.generateContent(
          "How do I make an appointment to renew my driver's license?");

      GroundingMetadata groundingMetadata = response.getCandidates(0).getGroundingMetadata();
      String answer = ResponseHandler.getText(response);

      System.out.println("Answer: " + answer);
      System.out.println("Grounding metadata: " + groundingMetadata);

      return answer;
    }
  }
}
// [END generativeaionvertexai_grounding_private_data_basic]