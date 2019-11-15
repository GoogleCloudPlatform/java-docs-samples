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

package com.google.cloud.automl.tables.samples;

// Imports the Google Cloud client library
import com.google.cloud.automl.v1beta1.AutoMlClient;
import com.google.cloud.automl.v1beta1.BigQueryDestination;
import com.google.cloud.automl.v1beta1.BigQuerySource;
import com.google.cloud.automl.v1beta1.ColumnSpec;
import com.google.cloud.automl.v1beta1.ColumnSpecName;
import com.google.cloud.automl.v1beta1.DataType;
import com.google.cloud.automl.v1beta1.Dataset;
import com.google.cloud.automl.v1beta1.DatasetName;
import com.google.cloud.automl.v1beta1.GcsDestination;
import com.google.cloud.automl.v1beta1.GcsSource;
import com.google.cloud.automl.v1beta1.GetColumnSpecRequest;
import com.google.cloud.automl.v1beta1.GetTableSpecRequest;
import com.google.cloud.automl.v1beta1.InputConfig;
import com.google.cloud.automl.v1beta1.ListColumnSpecsRequest;
import com.google.cloud.automl.v1beta1.ListDatasetsRequest;
import com.google.cloud.automl.v1beta1.ListTableSpecsRequest;
import com.google.cloud.automl.v1beta1.LocationName;
import com.google.cloud.automl.v1beta1.OutputConfig;
import com.google.cloud.automl.v1beta1.TableSpec;
import com.google.cloud.automl.v1beta1.TableSpecName;
import com.google.cloud.automl.v1beta1.TablesDatasetMetadata;
import com.google.cloud.automl.v1beta1.TypeCode;
import com.google.cloud.automl.v1beta1.UpdateColumnSpecRequest;
import com.google.cloud.automl.v1beta1.UpdateDatasetRequest;
import com.google.protobuf.Empty;
import com.google.protobuf.FieldMask;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutionException;

public class DatasetApi {

  // [START automl_tables_create_dataset]
  /**
   * Demonstrates using the AutoML client to create a dataset
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param datasetName the name of the dataset to be created.
   */
  public static void createDataset(String projectId, String computeRegion, String datasetName)
      throws IOException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {

      // A resource that represents Google Cloud Platform location.
      LocationName projectLocation = LocationName.of(projectId, computeRegion);

      // Specify the tables dataset metadata for the dataset.
      TablesDatasetMetadata tablesDatasetMetadata = TablesDatasetMetadata.newBuilder().build();

      // Set dataset name and dataset metadata.
      Dataset myDataset =
          Dataset.newBuilder()
              .setDisplayName(datasetName)
              .setTablesDatasetMetadata(tablesDatasetMetadata)
              .build();

      // Create a dataset with the dataset metadata in the region.
      Dataset dataset = client.createDataset(projectLocation, myDataset);

      // Display the dataset information.
      System.out.format("Dataset name: %s\n", dataset.getName());
      System.out.format(
          "Dataset Id: %s\n",
          dataset.getName().split("/")[dataset.getName().split("/").length - 1]);
    }
  }
  // [END automl_tables_create_dataset]

  // [START automl_tables_list_datasets]
  /**
   * Demonstrates using the AutoML client to list all datasets.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param filter the Filter expression.
   */
  public static void listDatasets(String projectId, String computeRegion, String filter)
      throws IOException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {

      // A resource that represents Google Cloud Platform location.
      LocationName projectLocation = LocationName.of(projectId, computeRegion);

      // Build the List datasets request.
      ListDatasetsRequest request =
          ListDatasetsRequest.newBuilder()
              .setParent(projectLocation.toString())
              .setFilter(filter)
              .build();

      // List all the datasets available in the region by applying filter.
      System.out.println("List of datasets:");
      for (Dataset dataset : client.listDatasets(request).iterateAll()) {
        // Display the dataset information.
        System.out.format("\nDataset name: %s\n", dataset.getName());
        System.out.format(
            "Dataset Id: %s\n",
            dataset.getName().split("/")[dataset.getName().split("/").length - 1]);
        System.out.format("Dataset display name: %s\n", dataset.getDisplayName());
        System.out.println("Tables dataset metadata:");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        System.out.format(
            "\tPrimary table spec id: %s\n",
            dataset.getTablesDatasetMetadata().getPrimaryTableSpecId());
        System.out.format(
            "\tTarget column spec id: %s\n",
            dataset.getTablesDatasetMetadata().getTargetColumnSpecId());
        String statsUpdateTime =
            dateFormat.format(
                new java.util.Date(
                    dataset.getTablesDatasetMetadata().getStatsUpdateTime().getSeconds() * 1000));
        System.out.format("\tStats update time: %s\n", statsUpdateTime);
        System.out.format("Dataset example count: %d\n", dataset.getExampleCount());
        String createTime =
            dateFormat.format(new java.util.Date(dataset.getCreateTime().getSeconds() * 1000));
        System.out.format("Dataset create time: %s\n", createTime);
      }
    }
  }
  // [END automl_tables_list_datasets]

  // [START automl_tables_get_dataset]
  /**
   * Demonstrates using the AutoML client to get a dataset by ID.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param datasetId the Id of the dataset.
   */
  public static void getDataset(String projectId, String computeRegion, String datasetId)
      throws IOException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {
      // Get the complete path of the dataset.
      DatasetName datasetFullId = DatasetName.of(projectId, computeRegion, datasetId);

      // Get all the information about a given dataset.
      Dataset dataset = client.getDataset(datasetFullId);

      // Display the dataset information.
      System.out.format("Dataset name: %s\n", dataset.getName());
      System.out.format(
          "Dataset Id: %s\n",
          dataset.getName().split("/")[dataset.getName().split("/").length - 1]);
      System.out.format("Dataset display name: %s\n", dataset.getDisplayName());
      System.out.println("Tables dataset metadata:");
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
      System.out.format(
          "\tPrimary table spec id: %s\n",
          dataset.getTablesDatasetMetadata().getPrimaryTableSpecId());
      System.out.format(
          "\tTarget column spec id: %s\n",
          dataset.getTablesDatasetMetadata().getTargetColumnSpecId());
      String statsUpdateTime =
          dateFormat.format(
              new java.util.Date(
                  dataset.getTablesDatasetMetadata().getStatsUpdateTime().getSeconds() * 1000));
      System.out.format("\tStats update time: %s\n", statsUpdateTime);
      System.out.format("Dataset example count: %d\n", dataset.getExampleCount());
      String createTime =
          dateFormat.format(new java.util.Date(dataset.getCreateTime().getSeconds() * 1000));
      System.out.format("Dataset create time: %s\n", createTime);
    }
  }
  // [END automl_tables_get_dataset]

  // [START automl_tables_import_data]
  /**
   * Demonstrates using the AutoML client to import data.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param datasetId the Id of the dataset into which the training content are to be imported.
   * @param path 'gs://YOUR_BUCKET_ID/path_to_csv_file' or 'bq://project_id.dataset_id.table_id'
   */
  public static void importData(
      String projectId, String computeRegion, String datasetId, String path)
      throws IOException, InterruptedException, ExecutionException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {
      // Get the complete path of the dataset.
      DatasetName datasetFullId = DatasetName.of(projectId, computeRegion, datasetId);

      InputConfig inputConfig = null;

      // Checking the source type.
      if (path.startsWith("bq")) {

        // Get training data file to be imported from bigquery source.
        BigQuerySource.Builder bigQuerySource = BigQuerySource.newBuilder();
        bigQuerySource.setInputUri(path);

        // Import data from the bigquery source input URI.
        inputConfig = InputConfig.newBuilder().setBigquerySource(bigQuerySource).build();

      } else {

        // Get multiple training data files to be imported from gcsSource.
        GcsSource.Builder gcsSource = GcsSource.newBuilder();
        String[] inputUris = path.split(",");
        for (String inputUri : inputUris) {
          gcsSource.addInputUris(inputUri);
        }

        // Import data from the gcs source input URI.
        inputConfig = InputConfig.newBuilder().setGcsSource(gcsSource).build();
      }

      System.out.println("Processing import...");

      Empty response = client.importDataAsync(datasetFullId, inputConfig).get();
      System.out.format("Dataset imported. %s\n", response);
    }
  }
  // [END automl_tables_import_data]

  // [START automl_tables_export_data_to_csv]
  /**
   * Demonstrates using the AutoML client to export a dataset to a Google Cloud Storage bucket.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param datasetId the Id of the dataset.
   * @param gcsUri the Destination URI (Google Cloud Storage)
   */
  public static void exportDataToCsv(
      String projectId, String computeRegion, String datasetId, String gcsUri)
      throws IOException, InterruptedException, ExecutionException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {
      // Get the complete path of the dataset.
      DatasetName datasetFullId = DatasetName.of(projectId, computeRegion, datasetId);

      // Set the output URI.
      GcsDestination gcsDestination =
          GcsDestination.newBuilder().setOutputUriPrefix(gcsUri).build();

      // Export the dataset to the output URI.
      OutputConfig outputConfig =
          OutputConfig.newBuilder().setGcsDestination(gcsDestination).build();
      System.out.format("Processing export...\n");

      // Export the dataset in the destination URI
      Empty response = client.exportDataAsync(datasetFullId, outputConfig).get();
      System.out.format("Dataset exported. %s\n", response);
    }
  }
  // [END automl_tables_export_data_to_csv]

  // [START automl_tables_export_data_to_bigquery]
  /**
   * Demonstrates using the AutoML client to export a dataset to a BigQuery.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param datasetId the Id of the dataset.
   * @param bigQueryUri the Destination URI (BigQuery)
   */
  public static void exportDataToBigQuery(
      String projectId, String computeRegion, String datasetId, String bigQueryUri)
      throws IOException, InterruptedException, ExecutionException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {

      // Get the complete path of the dataset.
      DatasetName datasetFullId = DatasetName.of(projectId, computeRegion, datasetId);

      Dataset dataset = client.getDataset(datasetFullId);

      // Set the output URI.
      BigQueryDestination bigqueryDestination =
          BigQueryDestination.newBuilder().setOutputUri(bigQueryUri).build();

      // Export the dataset to the output URI.
      OutputConfig outputConfig =
          OutputConfig.newBuilder().setBigqueryDestination(bigqueryDestination).build();
      System.out.format("Processing export...\n");
      // Export the dataset in the destination URI
      Empty response = client.exportDataAsync(dataset.getName(), outputConfig).get();
      System.out.format("Dataset exported. %s\n", response);
    }
  }
  // [END automl_tables_export_data_to_bigquery]

  // [START automl_tables_list_table_specs]
  /**
   * Demonstrates using the AutoML client to list all table specs in datasets.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param datasetId the Id of the dataset.
   * @param filter the Filter expression.
   */
  public static void listTableSpecs(
      String projectId, String computeRegion, String datasetId, String filter) throws IOException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {

      // Get the complete path of the dataset.
      DatasetName datasetFullId = DatasetName.of(projectId, computeRegion, datasetId);

      // Build the List table specs request.
      ListTableSpecsRequest request =
          ListTableSpecsRequest.newBuilder()
              .setParent(datasetFullId.toString())
              .setFilter(filter)
              .build();

      // List all the tableSpecs of particular dataset available in the region by
      // applying filter.
      System.out.println("List of table specs:");
      for (TableSpec table : client.listTableSpecs(request).iterateAll()) {
        // Display the table information.
        System.out.format("Table name: %s\n", table.getName());
        System.out.format(
            "Table Id: %s\n", table.getName().split("/")[table.getName().split("/").length - 1]);
        System.out.format("Table row count: %s\n", table.getRowCount());
        System.out.format("Table column count: %s\n", table.getColumnCount());
        System.out.format("Table input config:\n");
        if (table.getInputConfigs(0).getSourceCase().toString().equals("BIGQUERY_SOURCE")) {
          System.out.format("\t%s\n", table.getInputConfigs(0).getBigquerySource().getInputUri());
        } else {
          System.out.format(
              "\t%s\n", table.getInputConfigs(0).getGcsSource().getInputUrisList().toString());
        }
      }
    }
  }
  // [END automl_tables_list_table_specs]

  // [START automl_tables_get_table_spec]
  /**
   * Demonstrates using the AutoML client to get all table specs information in table.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param datasetId the Id of the dataset.
   * @param tableId the Id of the table.
   */
  public static void getTableSpec(
      String projectId, String computeRegion, String datasetId, String tableId) throws IOException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {

      // Get the complete path of the table.
      TableSpecName tableSpecId = TableSpecName.of(projectId, computeRegion, datasetId, tableId);

      // Build the get table spec request.
      GetTableSpecRequest request =
          GetTableSpecRequest.newBuilder().setName(tableSpecId.toString()).build();

      // Get all the information about a given tableSpec of particular dataset .
      TableSpec table = client.getTableSpec(request);

      // Display the table spec information.
      System.out.format("Table name: %s\n", table.getName());
      System.out.format(
          "Table Id: %s\n", table.getName().split("/")[table.getName().split("/").length - 1]);
      System.out.format("Table row count: %s\n", table.getRowCount());
      System.out.format("Table column count: %s\n", table.getColumnCount());
      System.out.format("Table input config:\n");
      if (table.getInputConfigs(0).getSourceCase().toString().equals("BIGQUERY_SOURCE")) {
        System.out.format("\t%s\n", table.getInputConfigs(0).getBigquerySource().getInputUri());
      } else {
        System.out.format(
            "\t%s\n", table.getInputConfigs(0).getGcsSource().getInputUrisList().toString());
      }
    }
  }
  // [END automl_tables_get_table_spec]

  // [START automl_tables_list_column_specs]
  /**
   * Demonstrates using the AutoML client to list all column specs in table columns.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param datasetId the Id of the dataset.
   * @param tableId the Id of the table.
   * @param filter the Filter expression.
   */
  public static void listColumnSpecs(
      String projectId, String computeRegion, String datasetId, String tableId, String filter)
      throws IOException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {

      // Get the complete path of the table.
      TableSpecName tableSpecId = TableSpecName.of(projectId, computeRegion, datasetId, tableId);

      // Build the List table specs request.
      ListColumnSpecsRequest request =
          ListColumnSpecsRequest.newBuilder()
              .setParent(tableSpecId.toString())
              .setFilter(filter)
              .build();

      // List all the column specs of particular table available in the region by
      // applying filter.
      System.out.println("List of column specs:");
      for (ColumnSpec column : client.listColumnSpecs(request).iterateAll()) {
        // Display the table columns information.
        System.out.format("\nColumn name: %s\n", column.getName());
        System.out.format(
            "Column Id: %s\n", column.getName().split("/")[column.getName().split("/").length - 1]);
        System.out.format("Column display name : %s\n", column.getDisplayName());
        System.out.format("Column data type : %s\n", column.getDataType().getTypeCode());
        System.out.format(
            "Column distinct value count : %s", column.getDataStats().getDistinctValueCount());
      }
    }
  }
  // [END automl_tables_list_column_specs]

  // [START automl_tables_get_column_spec]
  /**
   * Demonstrates using the AutoML client to get all column specs information in table colums.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param datasetId the Id of the dataset.
   * @param tableId the Id of the table.
   * @param columnId the Id of the column.
   */
  public static void getColumnSpec(
      String projectId, String computeRegion, String datasetId, String tableId, String columnId)
      throws IOException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {

      // Get the complete path of the column.
      ColumnSpecName columnSpecId =
          ColumnSpecName.of(projectId, computeRegion, datasetId, tableId, columnId);

      // Build the get column spec request.
      GetColumnSpecRequest request =
          GetColumnSpecRequest.newBuilder().setName(columnSpecId.toString()).build();

      // Get all the information about a given columnSpec of particular dataset.
      ColumnSpec column = client.getColumnSpec(request);

      // Display the column spec information.
      System.out.format("Column name: %s\n", column.getName());
      System.out.format(
          "Column Id: %s\n", column.getName().split("/")[column.getName().split("/").length - 1]);
      System.out.format("Column display name : %s\n", column.getDisplayName());
      System.out.format("Column data type : %s\n", column.getDataType().getTypeCode());
      System.out.format(
          "Column distinct value count : %s\n", column.getDataStats().getDistinctValueCount());
    }
  }
  // [END automl_tables_get_column_spec]

  // [START automl_tables_update_dataset]
  /**
   * Demonstrates using the AutoML client to update a dataset by ID.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param datasetId the Id of the dataset.
   */
  public static void updateDataset(
      String projectId, String computeRegion, String datasetId, String displayName)
      throws IOException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {

      // Get the complete path of the dataset.
      DatasetName datasetFullId = DatasetName.of(projectId, computeRegion, datasetId);

      // Update the display name of the dataset.
      Dataset dataset =
          Dataset.newBuilder()
              .setName(datasetFullId.toString())
              .setDisplayName(displayName)
              .build();

      // Add the update mask to particular field.
      FieldMask updateMask = FieldMask.newBuilder().addPaths("display_name").build();

      // Build the Update datasets request.
      UpdateDatasetRequest request =
          UpdateDatasetRequest.newBuilder().setDataset(dataset).setUpdateMask(updateMask).build();

      // Update the information about a given dataset.
      Dataset updateResponse = client.updateDataset(request);

      // Display the dataset information.
      System.out.format("Dataset name: %s\n", updateResponse.getName());
      System.out.format(
          "Dataset Id: %s\n",
          updateResponse.getName().split("/")[updateResponse.getName().split("/").length - 1]);
      System.out.format("Updated dataset display name: %s\n", updateResponse.getDisplayName());
      System.out.println("Tables dataset metadata:");
      System.out.format("\t%s\n", updateResponse.getTablesDatasetMetadata());
      System.out.format("Dataset example count: %d\n", updateResponse.getExampleCount());

      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
      String createTime =
          dateFormat.format(new java.util.Date(updateResponse.getCreateTime().getSeconds() * 1000));
      System.out.format("Dataset create time: %s\n", createTime);
    }
  }
  // [END automl_tables_update_dataset]

  // [START automl_tables_update_column_spec]
  /**
   * Demonstrates using the AutoML client to update a column by ID.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param datasetId the Id of the dataset.
   * @param tableId the Id of the table.
   * @param columnId the Id of the column.
   * @param dataTypeCode the data type of elements stored in the column.
   */
  public static void updateColumnSpec(
      String projectId,
      String computeRegion,
      String datasetId,
      String tableId,
      String columnId,
      String dataTypeCode)
      throws IOException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {

      // Get the complete path of the column.
      ColumnSpecName columnSpecId =
          ColumnSpecName.of(projectId, computeRegion, datasetId, tableId, columnId);

      // Set typecode of column to be changed.
      TypeCode typeCode = TypeCode.valueOf(dataTypeCode.toUpperCase());

      // Build the datatype.
      DataType dataType = DataType.newBuilder().setTypeCode(typeCode).build();

      // Update the datatype value of the column spec.
      ColumnSpec columnSpec =
          ColumnSpec.newBuilder().setName(columnSpecId.toString()).setDataType(dataType).build();

      // Add the update mask to particular field.
      FieldMask updateMask = FieldMask.newBuilder().addPaths("data_type").build();

      // Build the Update column request.
      UpdateColumnSpecRequest request =
          UpdateColumnSpecRequest.newBuilder()
              .setColumnSpec(columnSpec)
              .setUpdateMask(updateMask)
              .build();

      // Update the information about a given dataset.
      ColumnSpec updateResponse = client.updateColumnSpec(request);

      // Display the update column spec information.
      System.out.format("Column name: %s\n", updateResponse.getName());
      System.out.format(
          "Column Id: %s\n",
          updateResponse.getName().split("/")[updateResponse.getName().split("/").length - 1]);
      System.out.format("Column display name : %s\n", updateResponse.getDisplayName());
      System.out.format("Column data type : %s\n", updateResponse.getDataType().getTypeCode());
    }
  }
  // [END automl_tables_update_column_spec]

  // [START automl_tables_delete_dataset]
  /**
   * Demonstrates using the AutoML client to delete a dataset.
   *
   * @param projectId the Id of the project.
   * @param computeRegion the Region name. (e.g., "us-central1")
   * @param datasetId the Id of the dataset.
   */
  public static void deleteDataset(String projectId, String computeRegion, String datasetId)
      throws IOException, InterruptedException, ExecutionException {
    // Instantiates a client.
    try (AutoMlClient client = AutoMlClient.create()) {

      // Get the complete path of the dataset.
      DatasetName datasetFullId = DatasetName.of(projectId, computeRegion, datasetId);

      // Delete a dataset.
      Empty response = client.deleteDatasetAsync(datasetFullId).get();
      System.out.format("Dataset deleted. %s\n", response);
    }
  }
  // [END automl_tables_delete_dataset]
}
