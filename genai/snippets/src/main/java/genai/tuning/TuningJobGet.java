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

// [START googlegenaisdk_tuning_job_get]

import com.google.genai.Client;
import com.google.genai.types.GetTuningJobConfig;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.TunedModel;
import com.google.genai.types.TuningJob;
import java.util.Optional;

public class TuningJobGet {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    // E.g. tuningJobName =
    // "projects/123456789012/locations/us-central1/tuningJobs/123456789012345"
    String tuningJobName = "your-job-name";
    getTuningJob(tuningJobName);
  }

  // Shows how to get a tuning job
  public static Optional<String> getTuningJob(String tuningJobName) {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("us-central1")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      TuningJob tuningJob = client.tunings.get(tuningJobName, GetTuningJobConfig.builder().build());

      tuningJob.tunedModel().flatMap(TunedModel::model).ifPresent(System.out::println);
      tuningJob.tunedModel().flatMap(TunedModel::endpoint).ifPresent(System.out::println);
      tuningJob.experiment().ifPresent(System.out::println);
      // Example response:
      // projects/123456789012/locations/us-central1/models/6129850992130260992@1
      // projects/123456789012/locations/us-central1/endpoints/105055037499113472
      // projects/123456789012/locations/us-central1/metadataStores/default/contexts/experiment_id
      return tuningJob.name();
    }
  }
}
// [END googlegenaisdk_tuning_job_get]
