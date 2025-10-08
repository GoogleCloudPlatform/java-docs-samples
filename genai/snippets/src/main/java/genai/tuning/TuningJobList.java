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

// [START googlegenaisdk_tuning_job_list]

import com.google.genai.Client;
import com.google.genai.Pager;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.ListTuningJobsConfig;
import com.google.genai.types.TuningJob;

public class TuningJobList {

  public static void main(String[] args) {
    listTuningJob();
  }

  // Shows how to list the available tuning jobs
  public static Pager<TuningJob> listTuningJob() {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("us-central1")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      Pager<TuningJob> tuningJobs = client.tunings.list(ListTuningJobsConfig.builder().build());
      for (TuningJob job : tuningJobs) {
        job.name().ifPresent(System.out::println);
        // Example response:
        // projects/123456789012/locations/us-central1/tuningJobs/329583781566480384
      }

      return tuningJobs;
    }
  }
}
// [END googlegenaisdk_tuning_job_list]
