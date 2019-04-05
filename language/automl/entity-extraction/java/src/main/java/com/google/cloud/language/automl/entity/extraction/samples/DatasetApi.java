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

package com.google.cloud.language.automl.entity.extraction.samples;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutionException;

// Imports the Google Cloud client library
import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.Dataset;
import com.google.cloud.automl.v1beta1.DatasetName;
import com.google.cloud.automl.v1beta1.GcsDestination;
import com.google.cloud.automl.v1beta1.GcsSource;
import com.google.cloud.automl.v1beta1.InputConfig;
import com.google.cloud.automl.v1beta1.ListDatasetsRequest;
import com.google.cloud.automl.v1beta1.LocationName;
import com.google.cloud.automl.v1beta1.OutputConfig;
import com.google.cloud.automl.v1beta1.TextExtractionDatasetMetadata;
import com.google.protobuf.Empty;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * Google Cloud AutoML Natural Language Entity API sample application. Example usage: mvn package
 * exec:java
 * -Dexec.mainClass='com.google.cloud.language.automl.entity.extraction.samples.DatasetApi'
 * -Dexec.args='create_dataset [datasetName]'
 */
public class DatasetApi {

  // [START automl_natural_language_entity_create_dataset]
  /**
   * Demonstrates using the AutoML client to create a dataset
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param datasetName the name of the dataset to be created.
   * @throws IOException
   */
  public static void createDataset(String projectId, String computeRegion, String datasetName)
      throws IOException {
    // Instantiates a client
    AutoMlClient client = AutoMlClient.create();

    // A resource that represents Google Cloud Platform location.
    LocationName projectLocation = LocationName.of(projectId, computeRegion);

    // Specify the text extraction dataset metadata for the dataset.
    TextExtractionDatasetMetadata textExtractionDatasetMetadata =
        TextExtractionDatasetMetadata.newBuilder().build();

    // Set dataset name and dataset metadata.
    Dataset myDataset =
        Dataset.newBuilder()
            .setDisplayName(datasetName)
            .setTextExtractionDatasetMetadata(textExtractionDatasetMetadata)
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
    System.out.println("Text extraction dataset metadata:");
    System.out.print(String.format("\t%s", dataset.getTextExtractionDatasetMetadata()));
    System.out.println(String.format("Dataset example count: %d", dataset.getExampleCount()));
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    String createTime =
        dateFormat.format(new java.util.Date(dataset.getCreateTime().getSeconds() * 1000));
    System.out.println(String.format("Dataset create time: %s", createTime));
  }
  // [END automl_natural_language_entity_create_dataset]

  // [START automl_natural_language_entity_list_datasets]
  /**
   * Demonstrates using the AutoML client to list all datasets.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param filter the Filter expression.
   * @throws IOException
   */
  public static void listDatasets(String projectId, String computeRegion, String filter)
      throws IOException {
    // Instantiates a client
    AutoMlClient client = AutoMlClient.create();

    // A resource that represents Google Cloud Platform location.
    LocationName projectLocation = LocationName.of(projectId, computeRegion);

    // Build the List datasets request
    ListDatasetsRequest request =
        ListDatasetsRequest.newBuilder()
            .setParent(projectLocation.toString())
            .setFilter(filter)
            .build();

    // List all the datasets available in the region by applying filter.
    System.out.println("List of datasets:");
    for (Dataset dataset : client.listDatasets(request).iterateAll()) {
      // Display the dataset information.
      System.out.println(String.format("\nDataset name: %s", dataset.getName()));
      System.out.println(
          String.format(
              "Dataset Id: %s",
              dataset.getName().split("/")[dataset.getName().split("/").length - 1]));
      System.out.println(String.format("Dataset display name: %s", dataset.getDisplayName()));
      System.out.println("Text extraction dataset metadata:");
      System.out.print(String.format("\t%s", dataset.getTextExtractionDatasetMetadata()));
      System.out.println(String.format("Dataset example count: %d", dataset.getExampleCount()));
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
      String createTime =
          dateFormat.format(new java.util.Date(dataset.getCreateTime().getSeconds() * 1000));
      System.out.println(String.format("Dataset create time: %s", createTime));
    }
  }
  // [END automl_natural_language_entity_list_datasets]

  // [START automl_natural_language_entity_get_dataset]
  /**
   * Demonstrates using the AutoML client to get a dataset by ID.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param datasetId the Id of the dataset.
   * @throws IOException
   */
  public static void getDataset(String projectId, String computeRegion, String datasetId)
      throws IOException {
    // Instantiates a client
    AutoMlClient client = AutoMlClient.create();

    // Get the complete path of the dataset.
    DatasetName datasetFullId = DatasetName.of(projectId, computeRegion, datasetId);

    // Get all the information about a given dataset.
    Dataset dataset = client.getDataset(datasetFullId);

    // Display the dataset information.
    System.out.println(String.format("Dataset name: %s", dataset.getName()));
    System.out.println(
        String.format(
            "Dataset Id: %s",
            dataset.getName().split("/")[dataset.getName().split("/").length - 1]));
    System.out.println(String.format("Dataset display name: %s", dataset.getDisplayName()));
    System.out.println("Text extraction dataset metadata:");
    System.out.print(String.format("\t%s", dataset.getTextExtractionDatasetMetadata()));
    System.out.println(String.format("Dataset example count: %d", dataset.getExampleCount()));
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    String createTime =
        dateFormat.format(new java.util.Date(dataset.getCreateTime().getSeconds() * 1000));
    System.out.println(String.format("Dataset create time: %s", createTime));
  }
  // [END automl_natural_language_entity_get_dataset]

  // [START automl_natural_language_entity_import_data]
  /**
   * Demonstrates using the AutoML client to import labeled items.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param datasetId the Id of the dataset into which the training content are to be imported.
   * @param path the Google Cloud Storage URIs. Target files must be in AutoML Natural Language
   *     Entity CSV format.
   * @throws ExecutionException
   * @throws InterruptedException
   * @throws IOException
   */
  public static void importData(
      String projectId, String computeRegion, String datasetId, String path)
      throws InterruptedException, ExecutionException, IOException {
    // Instantiates a client
    AutoMlClient client = AutoMlClient.create();

    // Get the complete path of the dataset.
    DatasetName datasetFullId = DatasetName.of(projectId, computeRegion, datasetId);

    GcsSource.Builder gcsSource = GcsSource.newBuilder();

    // Get multiple training data files to be imported from gcsSource.
    String[] inputUris = path.split(",");
    for (String inputUri : inputUris) {
      gcsSource.addInputUris(inputUri);
    }

    // Import data from the input URI
    InputConfig inputConfig = InputConfig.newBuilder().setGcsSource(gcsSource).build();
    System.out.println("Processing import...");

    Empty response = client.importDataAsync(datasetFullId, inputConfig).get();
    System.out.println(String.format("Dataset imported. %s", response));
  }
  // [END automl_natural_language_entity_import_data]

  // [START automl_natural_language_entity_export_data]
  /**
   * Demonstrates using the AutoML client to export a dataset to a Google Cloud Storage bucket.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param datasetId the Id of the dataset.
   * @param gcsUri the Destination URI (Google Cloud Storage)
   * @throws IOException
   * @throws ExecutionException
   * @throws InterruptedException
   */
  public static void exportData(
      String projectId, String computeRegion, String datasetId, String gcsUri)
      throws IOException, InterruptedException, ExecutionException {
    // Instantiates a client
    AutoMlClient client = AutoMlClient.create();

    // Get the complete path of the dataset.
    DatasetName datasetFullId = DatasetName.of(projectId, computeRegion, datasetId);

    // Set the output URI.
    GcsDestination gcsDestination = GcsDestination.newBuilder().setOutputUriPrefix(gcsUri).build();

    // Export the dataset to the output URI.
    OutputConfig outputConfig = OutputConfig.newBuilder().setGcsDestination(gcsDestination).build();
    System.out.println("Processing export...");

    // Export the dataset in the destination URI
    Empty response = client.exportDataAsync(datasetFullId, outputConfig).get();
    System.out.println(String.format("Dataset exported. %s", response));
  }
  // [END automl_natural_language_entity_export_data]

  // [START automl_natural_language_entity_delete_dataset]
  /**
   * Demonstrates using the AutoML client to delete a dataset.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param datasetId the Id of the dataset.
   * @throws ExecutionException
   * @throws InterruptedException
   * @throws IOException
   */
  public static void deleteDataset(String projectId, String computeRegion, String datasetId)
      throws InterruptedException, ExecutionException, IOException {
    // Instantiates a client
    AutoMlClient client = AutoMlClient.create();

    // Get the complete path of the dataset.
    DatasetName datasetFullId = DatasetName.of(projectId, computeRegion, datasetId);

    // Delete a dataset.
    Empty response = client.deleteDatasetAsync(datasetFullId).get();
    System.out.println(String.format("Dataset deleted. %s", response));
  }
  // [END automl_natural_language_entity_delete_dataset]

  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException {
    DatasetApi datasetApi = new DatasetApi();
    datasetApi.argsHelper(args);
  }

  public void argsHelper(String[] args)
      throws IOException, InterruptedException, ExecutionException {
    ArgumentParser parser =
        ArgumentParsers.newFor("DatasetApi")
            .build()
            .defaultHelp(true)
            .description("Dataset API operations.");
    Subparsers subparsers = parser.addSubparsers().dest("command");

    Subparser createDatasetParser = subparsers.addParser("create_dataset");
    createDatasetParser.addArgument("datasetName");

    Subparser listDatasetsParser = subparsers.addParser("list_datasets");
    listDatasetsParser
        .addArgument("filter")
        .nargs("?")
        .setDefault("textExtractionDatasetMetadata:*");

    Subparser getDatasetParser = subparsers.addParser("get_dataset");
    getDatasetParser.addArgument("datasetId");

    Subparser importDataParser = subparsers.addParser("import_data");
    importDataParser.addArgument("datasetId");
    importDataParser
        .addArgument("path")
        .nargs("?")
        .setDefault("gs://cloud-ml-data/NL-entity/dataset.csv");

    Subparser exportDataParser = subparsers.addParser("export_data");
    exportDataParser.addArgument("datasetId");
    exportDataParser.addArgument("gcsUri");

    Subparser deleteDatasetParser = subparsers.addParser("delete_dataset");
    deleteDatasetParser.addArgument("datasetId");

    String projectId = System.getenv("PROJECT_ID");
    String computeRegion = System.getenv("REGION_NAME");

    Namespace ns = null;
    try {
      ns = parser.parseArgs(args);

      if (ns.get("command").equals("create_dataset")) {
        createDataset(projectId, computeRegion, ns.getString("datasetName"));
      }
      if (ns.get("command").equals("list_datasets")) {
        listDatasets(projectId, computeRegion, ns.getString("filter"));
      }
      if (ns.get("command").equals("get_dataset")) {
        getDataset(projectId, computeRegion, ns.getString("datasetId"));
      }
      if (ns.get("command").equals("import_data")) {
        importData(projectId, computeRegion, ns.getString("datasetId"), ns.getString("path"));
      }
      if (ns.get("command").equals("export_data")) {
        exportData(projectId, computeRegion, ns.getString("datasetId"), ns.getString("gcsUri"));
      }
      if (ns.get("command").equals("delete_dataset")) {
        deleteDataset(projectId, computeRegion, ns.getString("datasetId"));
      }
      System.exit(1);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.exit(1);
    }
  }
}
