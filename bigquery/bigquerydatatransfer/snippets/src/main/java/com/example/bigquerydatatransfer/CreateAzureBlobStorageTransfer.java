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

package com.example.bigquerydatatransfer;

// [START bigquerydatatransfer_create_azureblobstorage_transfer]

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.bigquery.datatransfer.v1.CreateTransferConfigRequest;
import com.google.cloud.bigquery.datatransfer.v1.DataTransferServiceClient;
import com.google.cloud.bigquery.datatransfer.v1.ProjectName;
import com.google.cloud.bigquery.datatransfer.v1.TransferConfig;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// Sample to create azure blob storage transfer config.
public class CreateAzureBlobStorageTransfer {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    final String projectId = "MY_PROJECT_ID";
    final String displayName = "MY_TRANSFER_DISPLAY_NAME";
    final String datasetId = "MY_DATASET_ID";
    String tableId = "MY_TABLE_ID";
    String storageAccount = "MY_AZURE_STORAGE_ACCOUNT_NAME";
    String containerName = "MY_AZURE_CONTAINER_NAME";
    String dataPath = "MY_AZURE_FILE_NAME_OR_PREFIX";
    String sasToken = "MY_AZURE_SAS_TOKEN";
    String fileFormat = "CSV";
    String fieldDelimiter = ",";
    String skipLeadingRows = "1";
    Map<String, Value> params = new HashMap<>();
    params.put(
        "destination_table_name_template", Value.newBuilder().setStringValue(tableId).build());
    params.put("storage_account", Value.newBuilder().setStringValue(storageAccount).build());
    params.put("container", Value.newBuilder().setStringValue(containerName).build());
    params.put("data_path", Value.newBuilder().setStringValue(dataPath).build());
    params.put("sas_token", Value.newBuilder().setStringValue(sasToken).build());
    params.put("file_format", Value.newBuilder().setStringValue(fileFormat).build());
    params.put("field_delimiter", Value.newBuilder().setStringValue(fieldDelimiter).build());
    params.put("skip_leading_rows", Value.newBuilder().setStringValue(skipLeadingRows).build());
    createAzureBlobStorageTransfer(projectId, displayName, datasetId, params);
  }

  public static void createAzureBlobStorageTransfer(
      String projectId, String displayName, String datasetId, Map<String, Value> params)
      throws IOException {
    TransferConfig transferConfig =
        TransferConfig.newBuilder()
            .setDestinationDatasetId(datasetId)
            .setDisplayName(displayName)
            .setDataSourceId("azure_blob_storage")
            .setParams(Struct.newBuilder().putAllFields(params).build())
            .setSchedule("every 24 hours")
            .build();
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (DataTransferServiceClient client = DataTransferServiceClient.create()) {
      ProjectName parent = ProjectName.of(projectId);
      CreateTransferConfigRequest request =
          CreateTransferConfigRequest.newBuilder()
              .setParent(parent.toString())
              .setTransferConfig(transferConfig)
              .build();
      TransferConfig config = client.createTransferConfig(request);
      System.out.println("Azure Blob Storage transfer created successfully: " + config.getName());
    } catch (ApiException ex) {
      System.out.print("Azure Blob Storage transfer was not created." + ex.toString());
    }
  }
}
// [END bigquerydatatransfer_create_azureblobstorage_transfer]
