/*
 * Copyright 2018 Google Inc.
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

package com.google.cloud.vision.samples.automl;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for Automl vision "Dataset API" sample. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class DatasetApiIT {

  private static final String PROJECT_ID = "java-docs-samples-testing";
  private static final String BUCKET = PROJECT_ID + "-vcm";
  private static final String COMPUTE_REGION = "us-central1";
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private String datasetId;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() {
    System.setOut(null);
  }

  @Test
  public void testCreateImportDeleteDataset() {
    // Create a random dataset name with a length of 32 characters (max allowed by AutoML)
    // To prevent name collisions when running tests in multiple java versions at once.
    // AutoML doesn't allow "-", but accepts "_"
    String datasetName = String.format("test_%s",
        UUID.randomUUID().toString().replace("-", "_").substring(0, 26));
    // Act
    DatasetApi.createDataset(PROJECT_ID, COMPUTE_REGION, datasetName, false);

    // Assert
    String got = bout.toString();
    datasetId =
        bout.toString()
            .split("\n")[0]
            .split("/")[(bout.toString().split("\n")[0]).split("/").length - 1];
    assertThat(got).contains("Dataset id:");

    // Act
    DatasetApi.importData(
        PROJECT_ID, COMPUTE_REGION, datasetId, "gs://" + BUCKET + "/flower_traindata.csv");

    // Assert
    got = bout.toString();
    assertThat(got).contains("Dataset id:");

    // Act
    DatasetApi.deleteDataset(PROJECT_ID, COMPUTE_REGION, datasetId);

    // Assert
    got = bout.toString();
    assertThat(got).contains("Dataset deleted.");
  }

  @Test
  public void testListGetDatasets() {
    // Act
    DatasetApi.listDatasets(PROJECT_ID, COMPUTE_REGION, "imageClassificationDatasetMetadata:*");

    // Assert
    String got = bout.toString();
    datasetId =
        bout.toString()
            .split("\n")[1]
            .split("/")[(bout.toString().split("\n")[1]).split("/").length - 1];
    assertThat(got).contains("Dataset id:");

    // Act
    DatasetApi.getDataset(PROJECT_ID, COMPUTE_REGION, datasetId);

    // Assert
    got = bout.toString();

    assertThat(got).contains("Dataset id:");
  }
}
