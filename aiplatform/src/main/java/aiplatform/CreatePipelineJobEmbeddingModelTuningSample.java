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
import com.google.cloud.aiplatform.v1beta1.CreatePipelineJobRequest;
import com.google.cloud.aiplatform.v1beta1.LocationName;
import com.google.cloud.aiplatform.v1beta1.PipelineJob;
import com.google.cloud.aiplatform.v1beta1.PipelineJob.RuntimeConfig;
import com.google.cloud.aiplatform.v1beta1.PipelineServiceClient;
import com.google.cloud.aiplatform.v1beta1.PipelineServiceSettings;
import com.google.protobuf.Value;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreatePipelineJobEmbeddingModelTuningSample {
  public static final String PIPELINE_TEMPLATE_URI =
      "https://us-kfp.pkg.dev/ml-pipeline/llm-text-embedding/tune-text-embedding-model/v1.1.2";
  public static final Pattern API_ENDPOINT_PATTERN =
      Pattern.compile("(?<Location>.+)(-autopush|-staging)?-aiplatform.+");

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String apiEndpoint = "us-central1-aiplatform.googleapis.com:443";
    String project = "PROJECT";
    String baseModelVersionId = "BASE_MODEL_VERSION_ID";
    String taskType = "TASK_TYPE";
    String pipelineJobDisplayName = "PIPELINE_JOB_DISPLAY_NAME";
    String modelDisplayName = "MODEL_DISPLAY_NAME";
    String outputDir = "OUTPUT_DIR";
    String queriesPath = "QUERIES";
    String corpusPath = "CORPUS";
    String trainLabelPath = "TRAIN_LABEL";
    String testLabelPath = "TEST_LABEL";
    int batchSize = 50;
    int iterations = 300;
    createEmbeddingModelTuningPipelineJob(
        apiEndpoint,
        project,
        baseModelVersionId,
        taskType,
        pipelineJobDisplayName,
        modelDisplayName,
        outputDir,
        queriesPath,
        corpusPath,
        trainLabelPath,
        testLabelPath,
        batchSize,
        iterations);
  }

  // Creates an embedding-model tuning pipeline job.
  public static void createEmbeddingModelTuningPipelineJob(
      String apiEndpoint,
      String project,
      String baseModelVersionId,
      String taskType,
      String pipelineJobDisplayName,
      String modelDisplayName,
      String outputDir,
      String queriesPath,
      String corpusPath,
      String trainLabelPath,
      String testLabelPath,
      int batchSize,
      int iterations)
      throws IOException {
    Matcher matcher = API_ENDPOINT_PATTERN.matcher(apiEndpoint);
    String location = matcher.matches() ? matcher.group("Location") : "us-central1";
    PipelineServiceSettings settings =
        PipelineServiceSettings.newBuilder().setEndpoint(apiEndpoint).build();
    try (PipelineServiceClient client = PipelineServiceClient.create(settings)) {
      Map<String, Value> parameterValues = Map.of(
          "project", valueOf(project),
          "base_model_version_id", valueOf(baseModelVersionId),
          "task_type", valueOf(taskType),
          "location", valueOf(location),
          "queries_path", valueOf(queriesPath),
          "corpus_path", valueOf(corpusPath),
          "train_label_path", valueOf(trainLabelPath),
          "test_label_path", valueOf(testLabelPath),
          "batch_size", valueOf(batchSize),
          "iterations", valueOf(iterations));
      PipelineJob pipelineJob =
          PipelineJob.newBuilder()
              .setTemplateUri(PIPELINE_TEMPLATE_URI)
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
      PipelineJob response = client.createPipelineJob(request);
      System.out.format("Got create respopnse: %s\n", response);
      System.out.format("Got pipeline job_name: %s\n", response.getName());
    }
  }

  private static Value valueOf(String s) {
    return Value.newBuilder().setStringValue(s).build();
  }

  private static Value valueOf(int n) {
    return Value.newBuilder().setNumberValue(n).build();
  }
}

 // [END aiplatform_sdk_embedding_model_tuning]
