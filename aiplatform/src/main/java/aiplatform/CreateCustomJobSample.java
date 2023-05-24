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
 */

package aiplatform;

// [START aiplatform_create_custom_job_sample]

import com.google.cloud.aiplatform.v1.AcceleratorType;
import com.google.cloud.aiplatform.v1.ContainerSpec;
import com.google.cloud.aiplatform.v1.CustomJob;
import com.google.cloud.aiplatform.v1.CustomJobSpec;
import com.google.cloud.aiplatform.v1.JobServiceClient;
import com.google.cloud.aiplatform.v1.JobServiceSettings;
import com.google.cloud.aiplatform.v1.LocationName;
import com.google.cloud.aiplatform.v1.MachineSpec;
import com.google.cloud.aiplatform.v1.WorkerPoolSpec;
import java.io.IOException;

// Create a custom job to run machine learning training code in Vertex AI
public class CreateCustomJobSample {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String project = "PROJECT";
    String displayName = "DISPLAY_NAME";

    // Vertex AI runs your training application in a Docker container image. A Docker container
    // image is a self-contained software package that includes code and all dependencies. Learn
    // more about preparing your training application at
    // https://cloud.google.com/vertex-ai/docs/training/overview#prepare_your_training_application
    String containerImageUri = "CONTAINER_IMAGE_URI";
    createCustomJobSample(project, displayName, containerImageUri);
  }

  static void createCustomJobSample(String project, String displayName, String containerImageUri)
      throws IOException {
    JobServiceSettings settings =
        JobServiceSettings.newBuilder()
            .setEndpoint("us-central1-aiplatform.googleapis.com:443")
            .build();
    String location = "us-central1";

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (JobServiceClient client = JobServiceClient.create(settings)) {
      MachineSpec machineSpec =
          MachineSpec.newBuilder()
              .setMachineType("n1-standard-4")
              .setAcceleratorType(AcceleratorType.NVIDIA_TESLA_K80)
              .setAcceleratorCount(1)
              .build();

      ContainerSpec containerSpec =
          ContainerSpec.newBuilder().setImageUri(containerImageUri).build();

      WorkerPoolSpec workerPoolSpec =
          WorkerPoolSpec.newBuilder()
              .setMachineSpec(machineSpec)
              .setReplicaCount(1)
              .setContainerSpec(containerSpec)
              .build();

      CustomJobSpec customJobSpecJobSpec =
          CustomJobSpec.newBuilder().addWorkerPoolSpecs(workerPoolSpec).build();

      CustomJob customJob =
          CustomJob.newBuilder()
              .setDisplayName(displayName)
              .setJobSpec(customJobSpecJobSpec)
              .build();
      LocationName parent = LocationName.of(project, location);
      CustomJob response = client.createCustomJob(parent, customJob);
      System.out.format("response: %s\n", response);
      System.out.format("Name: %s\n", response.getName());
    }
  }
}

// [END aiplatform_create_custom_job_sample]
