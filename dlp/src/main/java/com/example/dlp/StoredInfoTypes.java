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

package com.example.dlp;

import com.google.cloud.ServiceOptions;
import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.BigQueryField;
import com.google.privacy.dlp.v2.BigQueryTable;
import com.google.privacy.dlp.v2.CloudStorageFileSet;
import com.google.privacy.dlp.v2.CloudStoragePath;
import com.google.privacy.dlp.v2.CreateStoredInfoTypeRequest;
import com.google.privacy.dlp.v2.DeleteStoredInfoTypeRequest;
import com.google.privacy.dlp.v2.FieldId;
import com.google.privacy.dlp.v2.LargeCustomDictionaryConfig;
import com.google.privacy.dlp.v2.ListStoredInfoTypesRequest;
import com.google.privacy.dlp.v2.ProjectName;
import com.google.privacy.dlp.v2.ProjectStoredInfoTypeName;
import com.google.privacy.dlp.v2.StoredInfoType;
import com.google.privacy.dlp.v2.StoredInfoTypeConfig;
import com.google.privacy.dlp.v2.StoredInfoTypeVersion;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class StoredInfoTypes {

  // [START dlp_create_stored_info_type]
  /**
   * Create a DLP stored dictionary infoType from a set of GCS files.
   *
   * @param storedInfoTypeId (Optional) name of the stored infoType to be created
   * @param displayName (Optional) display name for the stored infoType to be created
   * @param description (Optional) description for the stored infoType to be created
   * @param inputFileBucket Name of GCS bucket containing dictionary word files
   * @param inputFileGlob Glob specifying dictionary word files in input file bucket
   * @param outputDataBucket Name of GCS bucket where the output data files should be saved
   * @param projectId The project ID to run the API call under
   */
  private static void createStoredInfoTypeFromGcsFiles(
      String storedInfoTypeId,
      String displayName,
      String description,
      String inputFileGcsPath,
      String outputGcsPath,
      String projectId)
      throws Exception {
    CloudStoragePath outputPath = CloudStoragePath.newBuilder().setPath(outputGcsPath).build();

    CloudStorageFileSet inputDataFiles =
        CloudStorageFileSet.newBuilder()
            .setUrl(inputFileGcsPath)
            .build();
    LargeCustomDictionaryConfig dictionaryConfig =
        LargeCustomDictionaryConfig.newBuilder()
            .setOutputPath(outputPath)
            .setCloudStorageFileSet(inputDataFiles)
            .build();
    createStoredInfoType(storedInfoTypeId, displayName, description, dictionaryConfig, projectId);
  }

  /**
   * Create a DLP stored dictionary infoType from a column in a BigQuery table.
   *
   * @param storedInfoTypeId (Optional) name of the stored infoType to be created
   * @param displayName (Optional) display name for the stored infoType to be created
   * @param description (Optional) description for the stored infoType to be created
   * @param bqInputTableId The project ID of the BigQuery table containing the dictionary words
   * @param bqInputDatasetId The BigQuery dataset of the table containing the dictionary words
   * @param bqInputDatasetId The table ID of the BigQuery table containing the dictionary words
   * @param bqInputTableField The field of the BigQuery table containing the dictionary words
   * @param inputFileGlob Glob specifying dictionary word files in input file bucket
   * @param outputDataBucket Name of GCS bucket where the output data files should be saved
   * @param projectId The project ID to run the API call under
   */
  private static void createStoredInfoTypeFromBigQueryTable(
      String storedInfoTypeId,
      String displayName,
      String description,
      String bqInputProjectId,
      String bqInputDatasetId,
      String bqInputTableId,
      String bqInputTableField,
      String outputGcsPath,
      String projectId)
      throws Exception {
    CloudStoragePath outputPath = CloudStoragePath.newBuilder().setPath(outputGcsPath).build();

    BigQueryTable bqTable =
        BigQueryTable.newBuilder()
            .setProjectId(bqInputProjectId)
            .setDatasetId(bqInputDatasetId)
            .setTableId(bqInputTableId)
            .build();
    BigQueryField bqField =
        BigQueryField.newBuilder()
            .setTable(bqTable)
            .setField(FieldId.newBuilder().setName(bqInputTableField).build())
            .build();
    LargeCustomDictionaryConfig dictionaryConfig =
        LargeCustomDictionaryConfig.newBuilder()
            .setOutputPath(outputPath)
            .setBigQueryField(bqField)
            .build();
    createStoredInfoType(storedInfoTypeId, displayName, description, dictionaryConfig, projectId);
  }

  /**
   * Create a DLP stored dictionary infoType from a dictionary config.
   *
   * @param storedInfoTypeId (Optional) name of the stored infoType to be created
   * @param displayName (Optional) display name for the stored infoType to be created
   * @param description (Optional) description for the stored infoType to be created
   * @param dictionaryConfig The configuration for the large custom dictionary
   * @param projectId The project ID to run the API call under
   */
  private static void createStoredInfoType(
      String storedInfoTypeId,
      String displayName,
      String description,
      LargeCustomDictionaryConfig dictionaryConfig,
      String projectId)
      throws Exception {

    // instantiate a client
    DlpServiceClient dlpServiceClient = DlpServiceClient.create();
    try {

      StoredInfoTypeConfig storedInfoTypeConfig =
          StoredInfoTypeConfig.newBuilder()
              .setDisplayName(displayName)
              .setDescription(description)
              .setLargeCustomDictionary(dictionaryConfig)
              .build();

      CreateStoredInfoTypeRequest createStoredInfoTypeRequest =
          CreateStoredInfoTypeRequest.newBuilder()
              .setParent(ProjectName.of(projectId).toString())
              .setConfig(storedInfoTypeConfig)
              .setStoredInfoTypeId(storedInfoTypeId)
              .build();

      StoredInfoType createdStoredInfoType =
          dlpServiceClient.createStoredInfoType(createStoredInfoTypeRequest);

      System.out.println("Created stored infoType: " + createdStoredInfoType.getName());
    } catch (Exception e) {
      System.out.println("Error creating stored infoType: " + e.getMessage());
    }
  }
  // [END dlp_create_stored_info_type]

  // [START dlp_list_stored_info_types]
  /**
   * List all DLP stored infoTypes for a given project.
   *
   * @param projectId The project ID to run the API call under.
   */
  private static void listStoredInfoTypes(String projectId) {
    // Instantiates a client
    try (DlpServiceClient dlpServiceClient = DlpServiceClient.create()) {
      ListStoredInfoTypesRequest listStoredInfoTypesRequest =
          ListStoredInfoTypesRequest.newBuilder()
              .setParent(ProjectName.of(projectId).toString())
              .build();
      DlpServiceClient.ListStoredInfoTypesPagedResponse response =
          dlpServiceClient.listStoredInfoTypes(listStoredInfoTypesRequest);
      response
          .getPage()
          .getValues()
          .forEach(
              storedInfoType -> {
                System.out.println("Stored infoType: " + storedInfoType.getName());
                if (storedInfoType.getCurrentVersion() != null) {
                  StoredInfoTypeVersion currentVersion = storedInfoType.getCurrentVersion();
                  System.out.println("Current version:");
                  System.out.println("\tCreated: " + currentVersion.getCreateTime());
                  System.out.println("\tState: " + currentVersion.getState());
                  System.out.println("\tError count: " + currentVersion.getErrorsCount());
                }
                if (storedInfoType.getPendingVersionsCount() > 0) {
                  System.out.println("Pending versions:");
                  for (StoredInfoTypeVersion pendingVersion :
                      storedInfoType.getPendingVersionsList()) {
                    System.out.println("\tCreated:" + pendingVersion.getCreateTime());
                    System.out.println("\tState: " + pendingVersion.getState());
                    System.out.println("\tError count: " + pendingVersion.getErrorsCount());
                  }
                }
              });
    } catch (Exception e) {
      System.out.println("Error listing stored infoTypes :" + e.getMessage());
    }
  }
  // [END dlp_list_stored_info_types]

  // [START dlp_delete_stored_info_type]
  /**
   * Delete a DLP stored infoType in a project.
   *
   * @param projectId The project ID to run the API call under.
   * @param storedInfoTypeId Stored infoType ID
   */
  private static void deleteStoredInfoType(String projectId, String storedInfoTypeId) {

    String storedInfoTypeName =
        ProjectStoredInfoTypeName.of(projectId, storedInfoTypeId).toString();
    try (DlpServiceClient dlpServiceClient = DlpServiceClient.create()) {
      DeleteStoredInfoTypeRequest deleteStoredInfoTypeRequest =
          DeleteStoredInfoTypeRequest.newBuilder().setName(storedInfoTypeName).build();
      dlpServiceClient.deleteStoredInfoType(deleteStoredInfoTypeRequest);

      System.out.println("Stored infoType deleted: " + storedInfoTypeName);
    } catch (Exception e) {
      System.out.println("Error deleting stored infoType :" + e.getMessage());
    }
  }
  // [END dlp_delete_stored_info_type]

  /** Command line application to create, list and delete stored infoTypes. */
  public static void main(String[] args) throws Exception {

    OptionGroup optionsGroup = new OptionGroup();
    optionsGroup.setRequired(true);

    Option createStoredInfoTypeOption =
        new Option("c", "create", false, "Create stored infoType from a list of words or phrases");
    optionsGroup.addOption(createStoredInfoTypeOption);

    Option listStoredInfoTypesOption = new Option("l", "list", false, "List stored infoTypes");
    optionsGroup.addOption(listStoredInfoTypesOption);

    Option deleteStoredInfoTypeOption = new Option("d", "delete", false, "Delete stored infoType");
    optionsGroup.addOption(deleteStoredInfoTypeOption);

    Options commandLineOptions = new Options();
    commandLineOptions.addOptionGroup(optionsGroup);

    Option gcsInputFilePathOption =
        Option.builder("gcsInputFilePath").hasArg(true).required(false).build();
    commandLineOptions.addOption(gcsInputFilePathOption);

    Option bqInputProjectIdOption =
        Option.builder("bqInputProjectId").hasArg(true).required(false).build();
    commandLineOptions.addOption(bqInputProjectIdOption);

    Option bqInputDatasetIdOption =
        Option.builder("bqInputDatasetId").hasArg(true).required(false).build();
    commandLineOptions.addOption(bqInputDatasetIdOption);

    Option bqInputTableIdOption =
        Option.builder("bqInputTableId").hasArg(true).required(false).build();
    commandLineOptions.addOption(bqInputTableIdOption);

    Option bqInputTableFieldOption =
        Option.builder("bqInputTableField").hasArg(true).required(false).build();
    commandLineOptions.addOption(bqInputTableFieldOption);

    Option gcsOutputPathOption =
        Option.builder("gcsOutputPath").hasArg(true).required(false).build();
    commandLineOptions.addOption(gcsOutputPathOption);

    Option projectIdOption =
        Option.builder("projectId").hasArg(true).required(false).build();
    commandLineOptions.addOption(projectIdOption);

    Option storedInfoTypeIdOption =
        Option.builder("storedInfoTypeId").hasArg(true).required(false).build();
    commandLineOptions.addOption(storedInfoTypeIdOption);
    Option displayNameOption = Option.builder("displayName").hasArg(true).required(false).build();
    commandLineOptions.addOption(displayNameOption);
    Option descriptionOption = Option.builder("description").hasArg(true).required(false).build();
    commandLineOptions.addOption(descriptionOption);

    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd;

    try {
      cmd = parser.parse(commandLineOptions, args);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp(StoredInfoTypes.class.getName(), commandLineOptions);
      System.exit(1);
      return;
    }

    String projectId =
        cmd.getOptionValue(projectIdOption.getOpt(), ServiceOptions.getDefaultProjectId());
    if (cmd.hasOption("c")) {
      String storedInfoTypeId = cmd.getOptionValue(storedInfoTypeIdOption.getOpt());
      String displayName = cmd.getOptionValue(displayNameOption.getOpt(), "");
      String description = cmd.getOptionValue(descriptionOption.getOpt(), "");
      String gcsOutputPath = cmd.getOptionValue(gcsOutputPathOption.getOpt());
      if (cmd.hasOption(gcsInputFilePathOption.getOpt())) {
        String gcsInputFilePath = cmd.getOptionValue(gcsInputFilePathOption.getOpt());
        createStoredInfoTypeFromGcsFiles(
            storedInfoTypeId,
            displayName,
            description,
            gcsInputFilePath,
            gcsOutputPath,
            projectId);
      } else {
        String bqInputProjectId = cmd.getOptionValue(bqInputProjectIdOption.getOpt(), projectId);
        String bqInputDatasetId = cmd.getOptionValue(bqInputDatasetIdOption.getOpt());
        String bqInputTableId = cmd.getOptionValue(bqInputTableIdOption.getOpt());
        String bqInputTableField = cmd.getOptionValue(bqInputTableFieldOption.getOpt());
        createStoredInfoTypeFromBigQueryTable(
            storedInfoTypeId,
            displayName,
            description,
            bqInputProjectId,
            bqInputDatasetId,
            bqInputTableId,
            bqInputTableField,
            gcsOutputPath,
            projectId);
      }
    } else if (cmd.hasOption("l")) {
      // list stored infoTypes
      listStoredInfoTypes(projectId);
    } else if (cmd.hasOption("d")) {
      String storedInfoTypeId = cmd.getOptionValue(storedInfoTypeIdOption.getOpt());
      deleteStoredInfoType(projectId, storedInfoTypeId);
    }
  }
}
