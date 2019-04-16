/*
 * Copyright 2019 Google LLC
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

package com.google.cloud.language.automl.sentiment.analysis.samples;

// [START automl_natural_language_sentiment_create_dataset]
import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.Dataset;
import com.google.cloud.automl.v1beta1.LocationName;
import com.google.cloud.automl.v1beta1.TextSentimentDatasetMetadata;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

class CreateDataset {

  // Create a dataset
  static void createDataset(
      String projectId, String computeRegion, String datasetName, String sentimentMax)
      throws IOException {
    // String projectId = "YOUR_PROJECT_ID";
    // String computeRegion = "YOUR_COMPUTE_REGION";
    // String datasetName = "YOUR_DATASET_DISPLAY_NAME";
    // String sentimentMax = "YOUR_SENTIMENT_VALUE";

    // Instantiates a client.
    AutoMlClient client = AutoMlClient.create();

    // A resource that represents Google Cloud Platform location.
    LocationName projectLocation = LocationName.of(projectId, computeRegion);

    // Specify the text sentiment dataset metadata for the dataset.
    TextSentimentDatasetMetadata textSentimentDatasetMetadata =
        TextSentimentDatasetMetadata.newBuilder()
            .setSentimentMax(Integer.valueOf(sentimentMax))
            .build();

    // Set dataset name and dataset metadata.
    Dataset myDataset =
        Dataset.newBuilder()
            .setDisplayName(datasetName)
            .setTextSentimentDatasetMetadata(textSentimentDatasetMetadata)
            .build();

    // Create a dataset with the dataset metadata in the region.
    Dataset dataset = client.createDataset(projectLocation, myDataset);

    // Display the dataset information.
    System.out.println(String.format("Dataset name: %s", dataset.getName()));
    System.out.println(
        String.format(
            "Dataset Id: %s",
            dataset.getName().split("/")[dataset.getName().split("/").length - 1]));
    System.out.println(String.format("Dataset display name: %s", dataset.getDisplayName()));
    System.out.println("Text sentiment dataset metadata:");
    System.out.print(String.format("\t%s", dataset.getTextSentimentDatasetMetadata()));
    System.out.println(String.format("Dataset example count: %d", dataset.getExampleCount()));
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    String createTime =
        dateFormat.format(new java.util.Date(dataset.getCreateTime().getSeconds() * 1000));
    System.out.println(String.format("Dataset create time: %s", createTime));
  }
}
// [END automl_natural_language_sentiment_create_dataset]
