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
 *
 *
 * List available featurestore details. See
 * https://cloud.google.com/vertex-ai/docs/featurestore/setup before running
 * the code snippet
 */

package aiplatform;

// [START aiplatform_sdk_list_tuned_models]

import com.google.cloud.aiplatform.v1beta1.ListModelsRequest;
import com.google.cloud.aiplatform.v1beta1.LocationName;
import com.google.cloud.aiplatform.v1beta1.Model;
import com.google.cloud.aiplatform.v1beta1.ModelServiceClient;
import com.google.cloud.aiplatform.v1beta1.ModelServiceClient.ListModelsPagedResponse;
import com.google.cloud.aiplatform.v1beta1.ModelServiceSettings;
import java.io.IOException;

public class ListTunedModelsSample {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace this variable before running the sample.
    String project = "YOUR_PROJECT_ID";

    String location = "us-central1";
    String model = "text-bison@001";

    listTunedModelsSample(project, location, model);
  }

  // List tuned models for a large language model
  public static void listTunedModelsSample(String project, String location, String model)
      throws IOException {
    final String endpoint = String.format("%s-aiplatform.googleapis.com:443", location);
    ModelServiceSettings modelServiceSettings =
        ModelServiceSettings.newBuilder().setEndpoint(endpoint).build();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (ModelServiceClient modelServiceClient = ModelServiceClient.create(modelServiceSettings)) {
      final String parent = LocationName.of(project, location).toString();
      final String filter =
          String.format("labels.google-vertex-llm-tuning-base-model-id=%s", model);
      ListModelsRequest request =
          ListModelsRequest.newBuilder().setParent(parent).setFilter(filter).build();

      ListModelsPagedResponse listModelsPagedResponse = modelServiceClient.listModels(request);
      System.out.println("List Tuned Models response");
      for (Model element : listModelsPagedResponse.iterateAll()) {
        System.out.format("\tModel Name: %s\n", element.getName());
        System.out.format("\tModel Display Name: %s\n", element.getDisplayName());
      }
    }
  }
}
// [END aiplatform_sdk_list_tuned_models]
