// Copyright 2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.dataflow;

import static junit.framework.TestCase.assertNotNull;

import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.testing.RemoteStorageHelper;
import com.google.dataflow.v1beta3.FlexTemplatesServiceClient;
import com.google.dataflow.v1beta3.LaunchFlexTemplateParameter;
import com.google.dataflow.v1beta3.LaunchFlexTemplateRequest;
import com.google.dataflow.v1beta3.LaunchFlexTemplateResponse;
import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient;
import com.google.devtools.artifactregistry.v1.CreateRepositoryRequest;
import com.google.devtools.artifactregistry.v1.DeleteRepositoryRequest;
import com.google.devtools.artifactregistry.v1.LocationName;
import com.google.devtools.artifactregistry.v1.Repository;
import com.google.devtools.artifactregistry.v1.Repository.Format;
import com.google.devtools.artifactregistry.v1.RepositoryName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.core.annotation.Order;

@RunWith(JUnit4.class)
public class FlexTemplateGettingStartedIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGION = "us-central1";

  private static Storage storage;
  private static String bucketName;
  private static final String repositoryName = "test-repo"
      + UUID.randomUUID().toString().substring(0, 8);
  private static String templatePath;
  private static String imagePath;

  private ByteArrayOutputStream bout;

  // Check if required environment variables are set.
  private static void requireEnv(String varName) {
    assertNotNull(
        String.format("Environment variable '%s' is required to perform these tests.", varName),
        System.getenv(varName));
  }

  @BeforeClass
  public static void setUp()
      throws IOException, ExecutionException, InterruptedException {

    requireEnv("GOOGLE_CLOUD_PROJECT");

    // Create the Cloud Storage bucket for the template file.
    RemoteStorageHelper helper = RemoteStorageHelper.create();
    storage = helper.getOptions().getService();
    bucketName = RemoteStorageHelper.generateBucketName();
    storage.create(BucketInfo.of(bucketName));

    // Create the artifact repository for the template artifact.
    try (ArtifactRegistryClient artifactRegistryClient = ArtifactRegistryClient.create()) {
      CreateRepositoryRequest request =
          CreateRepositoryRequest.newBuilder()
              .setParent(LocationName.of(PROJECT_ID, REGION).toString())
              .setRepositoryId(repositoryName)
              .setRepository(
                  Repository.newBuilder()
                      .setFormat(Format.DOCKER)
                      .build())
              .build();
      artifactRegistryClient.createRepositoryAsync(request).get();
    }

    templatePath = String.format("gs://%s/getting_started_java.json", bucketName);
    imagePath = String.format(
        "%s-docker.pkg.dev/%s/%s/dataflow/getting-started-java:%s",
        REGION, PROJECT_ID, repositoryName, UUID.randomUUID());
  }

  @AfterClass
  public static void tearDown() throws ExecutionException, InterruptedException, IOException {
    // Delete the storage bucket.
    RemoteStorageHelper.forceDelete(storage, bucketName, 5, TimeUnit.SECONDS);

    // Delete the Artifact Registry repository.
    try (ArtifactRegistryClient artifactRegistryClient = ArtifactRegistryClient.create()) {
      DeleteRepositoryRequest request =
          DeleteRepositoryRequest.newBuilder()
              .setName(RepositoryName.of(PROJECT_ID, REGION, repositoryName).toString())
              .build();
      artifactRegistryClient.deleteRepositoryAsync(request).get();
    }
  }

  @Test
  @Order(1)
  public void testBuildTemplate() throws IOException, InterruptedException {
    String[] flexTemplateBuildCmd =
        new String[]{
            "gcloud",
            "dataflow",
            "flex-template",
            "build",
            templatePath,
            "--image-gcr-path",
            imagePath,
            "--sdk-language",
            "JAVA",
            "--flex-template-base-image", "JAVA11",
            "--metadata-file", "metadata.json",
            "--jar", "target/flex-template-getting-started-1.0.jar",
            "--env",
            "FLEX_TEMPLATE_JAVA_MAIN_CLASS=\"com.example.dataflow.FlexTemplateGettingStarted\""
        };

    ProcessBuilder builder = new ProcessBuilder(flexTemplateBuildCmd);
    builder.redirectErrorStream(true);
    Process process = builder.start();

    // Verify the gcloud command completed successfully.
    int result = process.waitFor();
    Assert.assertEquals(0, result);
  }


  @Test
  @Order(2)
  public void testRunTemplate() throws IOException, InterruptedException {
    try (FlexTemplatesServiceClient flexTemplatesServiceClient =
        FlexTemplatesServiceClient.create()) {
      LaunchFlexTemplateParameter launchParameters =
          LaunchFlexTemplateParameter.newBuilder()
              .setJobName("job1")
              .setContainerSpecGcsPath(templatePath)
              .putParameters("output", String.format("gs://%s/out-", bucketName))
              .build();
      LaunchFlexTemplateRequest request =
          LaunchFlexTemplateRequest.newBuilder()
              .setProjectId(PROJECT_ID)
              .setLaunchParameter(launchParameters)
              .setLocation(REGION)
              .setValidateOnly(true)  // Dry run to validate the Dataflow job
              .build();
      LaunchFlexTemplateResponse response = flexTemplatesServiceClient.launchFlexTemplate(request);
    }
  }
}
