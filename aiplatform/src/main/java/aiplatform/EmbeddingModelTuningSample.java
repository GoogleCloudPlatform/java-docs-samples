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

package aiplatform;

// [START aiplatform_sdk_embedding_model_tuning]
// [START generativeaionvertexai_sdk_embedding_model_tuning]
import com.google.cloud.aiplatform.v1.CreatePipelineJobRequest;
import com.google.cloud.aiplatform.v1.LocationName;
import com.google.cloud.aiplatform.v1.PipelineJob;
import com.google.cloud.aiplatform.v1.PipelineJob.RuntimeConfig;
import com.google.cloud.aiplatform.v1.PipelineServiceClient;
import com.google.cloud.aiplatform.v1.PipelineServiceSettings;
import com.google.protobuf.Value;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmbeddingModelTuningSample {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running this sample.
    String apiEndpoint = "us-central1-aiplatform.googleapis.com:443";
    String project = "PROJECT";
    String baseModelVersionId = "BASE_MODEL_VERSION_ID";
    String taskType = "DEFAULT";
    String pipelineJobDisplayName = "PIPELINE_JOB_DISPLAY_NAME";
    String outputDir = "OUTPUT_DIR";
    String queriesPath = "QUERIES_PATH";
    String corpusPath = "CORPUS_PATH";
    String trainLabelPath = "TRAIN_LABEL_PATH";
    String testLabelPath = "TEST_LABEL_PATH";
    double learningRateMultiplier = 1.0;
    int outputDimensionality = 768;
    int batchSize = 128;
    int trainSteps = 1000;

    createEmbeddingModelTuningPipelineJob(
        apiEndpoint,
        project,
        baseModelVersionId,
        taskType,
        pipelineJobDisplayName,
        outputDir,
        queriesPath,
        corpusPath,
        trainLabelPath,
        testLabelPath,
        learningRateMultiplier,
        outputDimensionality,
        batchSize,
        trainSteps);
  }

  public static PipelineJob createEmbeddingModelTuningPipelineJob(
      String apiEndpoint,
      String project,
      String baseModelVersionId,
      String taskType,
      String pipelineJobDisplayName,
      String outputDir,
      String queriesPath,
      String corpusPath,
      String trainLabelPath,
      String testLabelPath,
      double learningRateMultiplier,
      int outputDimensionality,
      int batchSize,
      int trainSteps)
      throws IOException {
    Matcher matcher = Pattern.compile("^(?<Location>\\w+-\\w+)").matcher(apiEndpoint);
    String location = matcher.matches() ? matcher.group("Location") : "us-central1";
    String templateUri =
        "https://us-kfp.pkg.dev/ml-pipeline/llm-text-embedding/tune-text-embedding-model/v1.1.3";
    PipelineServiceSettings settings =
        PipelineServiceSettings.newBuilder().setEndpoint(apiEndpoint).build();
    try (PipelineServiceClient client = PipelineServiceClient.create(settings)) {
      Map<String, Value> parameterValues =
          Map.of(
              "base_model_version_id", valueOf(baseModelVersionId),
              "task_type", valueOf(taskType),
              "queries_path", valueOf(queriesPath),
              "corpus_path", valueOf(corpusPath),
              "train_label_path", valueOf(trainLabelPath),
              "test_label_path", valueOf(testLabelPath),
              "learning_rate_multiplier", valueOf(learningRateMultiplier),
              "output_dimensionality", valueOf(outputDimensionality),
              "batch_size", valueOf(batchSize),
              "train_steps", valueOf(trainSteps));
      PipelineJob pipelineJob =
          PipelineJob.newBuilder()
              .setTemplateUri(templateUri)
              .setDisplayName(pipelineJobDisplayName)
              .setRuntimeConfig(
                  RuntimeConfig.newBuilder()
                      .setGcsOutputDirectory(outputDir)
                      .putAllParameterValues(parameterValues)
                      .build())
              .build();
      CreatePipelineJobRequest request =
          CreatePipelineJobRequest.newBuilder()
              .setParent(LocationName.of(project, location).toString())
              .setPipelineJob(pipelineJob)
              .build();
      return client.createPipelineJob(request);
    }
  }

  private static Value valueOf(String s) {
    return Value.newBuilder().setStringValue(s).build();
  }

  private static Value valueOf(int n) {
    return Value.newBuilder().setNumberValue(n).build();
  }

  private static Value valueOf(double n) {
    return Value.newBuilder().setNumberValue(n).build();
  }
}
// [END aiplatform_sdk_embedding_model_tuning]
// [END generativeaionvertexai_sdk_embedding_model_tuning]
