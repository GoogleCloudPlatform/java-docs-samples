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

// [START googlegenaisdk_tuning_job_create]

import static com.google.genai.types.JobState.Known.JOB_STATE_PENDING;
import static com.google.genai.types.JobState.Known.JOB_STATE_RUNNING;

import com.google.genai.Client;
import com.google.genai.types.CreateTuningJobConfig;
import com.google.genai.types.GetTuningJobConfig;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.JobState;
import com.google.genai.types.TunedModel;
import com.google.genai.types.TunedModelCheckpoint;
import com.google.genai.types.TuningDataset;
import com.google.genai.types.TuningJob;
import com.google.genai.types.TuningValidationDataset;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TuningJobCreate {

  public static void main(String[] args) throws InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    String model = "gemini-2.5-flash";
    createTuningJob(model);
  }

  // Shows how to create a supervised fine-tuning job using training and validation datasets
  public static String createTuningJob(String model) throws InterruptedException {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("us-central1")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1beta1").build())
            .build()) {

      String trainingDatasetUri =
          "gs://cloud-samples-data/ai-platform/generative_ai/gemini/text/sft_train_data.jsonl";
      TuningDataset trainingDataset = TuningDataset.builder().gcsUri(trainingDatasetUri).build();

      String validationDatasetUri =
          "gs://cloud-samples-data/ai-platform/generative_ai/gemini/text/sft_validation_data.jsonl";
      TuningValidationDataset validationDataset =
          TuningValidationDataset.builder().gcsUri(validationDatasetUri).build();

      TuningJob tuningJob =
          client.tunings.tune(
              model,
              trainingDataset,
              CreateTuningJobConfig.builder()
                  .tunedModelDisplayName("your-display-name")
                  .validationDataset(validationDataset)
                  .build());

      String jobName =
          tuningJob.name().orElseThrow(() -> new IllegalStateException("Missing job name"));
      Optional<JobState> jobState = tuningJob.state();
      Set<JobState.Known> runningStates = EnumSet.of(JOB_STATE_PENDING, JOB_STATE_RUNNING);

      while (jobState.isPresent() && runningStates.contains(jobState.get().knownEnum())) {
        System.out.println("Job state: " + jobState.get());
        tuningJob = client.tunings.get(jobName, GetTuningJobConfig.builder().build());
        jobState = tuningJob.state();
        TimeUnit.SECONDS.sleep(60);
      }

      tuningJob.tunedModel().flatMap(TunedModel::model).ifPresent(System.out::println);
      tuningJob.tunedModel().flatMap(TunedModel::endpoint).ifPresent(System.out::println);
      tuningJob.experiment().ifPresent(System.out::println);
      // Example response:
      // projects/123456789012/locations/us-central1/models/6129850992130260992@1
      // projects/123456789012/locations/us-central1/endpoints/105055037499113472
      // projects/123456789012/locations/us-central1/metadataStores/default/contexts/experiment_id

      List<TunedModelCheckpoint> checkpoints =
          tuningJob.tunedModel().flatMap(TunedModel::checkpoints).orElse(Collections.emptyList());

      int index = 0;
      for (TunedModelCheckpoint checkpoint : checkpoints) {
        System.out.println("Checkpoint " + (++index));
        checkpoint
            .checkpointId()
            .ifPresent(checkpointId -> System.out.println("checkpointId=" + checkpointId));
        checkpoint.epoch().ifPresent(epoch -> System.out.println("epoch=" + epoch));
        checkpoint.step().ifPresent(step -> System.out.println("step=" + step));
        checkpoint.endpoint().ifPresent(endpoint -> System.out.println("endpoint=" + endpoint));
      }
      // Example response:
      // Checkpoint 1
      // checkpointId=1
      // epoch=2
      // step=34
      // endpoint=projects/project/locations/location/endpoints/105055037499113472
      // ...
      return jobName;
    }
  }
}
// [END googlegenaisdk_tuning_job_create]
