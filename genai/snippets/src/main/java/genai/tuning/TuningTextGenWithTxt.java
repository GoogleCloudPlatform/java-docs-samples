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

package genai.tuning;

// [START googlegenaisdk_tuning_textgen_with_txt]

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GetTuningJobConfig;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.TunedModel;
import com.google.genai.types.TuningJob;

public class TuningTextGenWithTxt {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    // E.g. tuningJobName =
    // "projects/123456789012/locations/us-central1/tuningJobs/123456789012345"
    String tuningJobName = "your-job-name";
    predictWithTunedEndpoint(tuningJobName);
  }

  // Shows how to predict with a tuned model endpoint
  public static String predictWithTunedEndpoint(String tuningJobName) {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("us-central1")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      TuningJob tuningJob = client.tunings.get(tuningJobName, GetTuningJobConfig.builder().build());

      String endpoint =
          tuningJob
              .tunedModel()
              .flatMap(TunedModel::endpoint)
              .orElseThrow(() -> new IllegalStateException("Missing tuned model endpoint"));

      GenerateContentResponse response =
          client.models.generateContent(
              endpoint, "Why is the sky blue?", GenerateContentConfig.builder().build());

      System.out.println(response.text());
      // Example response:
      // The sky is blue because of a phenomenon called Rayleigh scattering...
      return response.text();
    }
  }
}
// [END googlegenaisdk_tuning_textgen_with_txt]
