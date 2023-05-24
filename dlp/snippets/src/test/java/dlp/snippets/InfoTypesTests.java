/*
 * Copyright 2020 Google Inc.
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

package dlp.snippets;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.common.collect.ImmutableList;
import com.google.privacy.dlp.v2.BigQueryField;
import com.google.privacy.dlp.v2.BigQueryTable;
import com.google.privacy.dlp.v2.CloudStoragePath;
import com.google.privacy.dlp.v2.CreateStoredInfoTypeRequest;
import com.google.privacy.dlp.v2.DeleteStoredInfoTypeRequest;
import com.google.privacy.dlp.v2.FieldId;
import com.google.privacy.dlp.v2.LargeCustomDictionaryConfig;
import com.google.privacy.dlp.v2.LocationName;
import com.google.privacy.dlp.v2.ProjectStoredInfoTypeName;
import com.google.privacy.dlp.v2.StoredInfoTypeConfig;
import java.io.IOException;
import java.util.UUID;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class InfoTypesTests extends TestBase {

  private static DlpServiceClient DLP_SERVICE_CLIENT;

  @BeforeClass
  public static void setUp() throws Exception {
    // Initialize the Dlp Service Client.
    DLP_SERVICE_CLIENT = DlpServiceClient.create();
  }

  public static void createStoredInfoType(String projectId, String outputPath, String infoTypeId) {
    // Optionally set a display name and a description.
    String displayName = "GitHub usernames";
    String description = "Dictionary of GitHub usernames used in commits";

    CloudStoragePath cloudStoragePath = CloudStoragePath.newBuilder().setPath(outputPath).build();

    BigQueryTable table =
        BigQueryTable.newBuilder()
            .setProjectId("bigquery-public-data")
            .setTableId("github_nested")
            .setDatasetId("samples")
            .build();

    BigQueryField bigQueryField =
        BigQueryField.newBuilder()
            .setTable(table)
            .setField(FieldId.newBuilder().setName("actor").build())
            .build();

    LargeCustomDictionaryConfig largeCustomDictionaryConfig =
        LargeCustomDictionaryConfig.newBuilder()
            .setOutputPath(cloudStoragePath)
            .setBigQueryField(bigQueryField)
            .build();

    StoredInfoTypeConfig storedInfoTypeConfig =
        StoredInfoTypeConfig.newBuilder()
            .setDisplayName(displayName)
            .setDescription(description)
            .setLargeCustomDictionary(largeCustomDictionaryConfig)
            .build();

    // Combine configurations into a request for the service.
    CreateStoredInfoTypeRequest createStoredInfoType =
        CreateStoredInfoTypeRequest.newBuilder()
            .setParent(LocationName.of(projectId, "global").toString())
            .setConfig(storedInfoTypeConfig)
            .setStoredInfoTypeId(infoTypeId)
            .build();

    // Send the request and receive response from the service.
    DLP_SERVICE_CLIENT.createStoredInfoType(createStoredInfoType);
  }

  @Override
  protected ImmutableList<String> requiredEnvVars() {
    return ImmutableList.of("GOOGLE_APPLICATION_CREDENTIALS", "GOOGLE_CLOUD_PROJECT", "GCS_PATH");
  }

  @Test
  public void testListInfoTypes() throws Exception {
    InfoTypesList.listInfoTypes();
    String output = bout.toString();
    assertThat(output).contains("Name");
    assertThat(output).contains("Display name");
  }

  @Test
  public void testCreateStoredInfoType() throws IOException {
    CreateStoredInfoType.createStoredInfoType(PROJECT_ID, GCS_PATH);
    String output = bout.toString();
    assertThat(output).contains("Created Stored InfoType: ");
    String storedInfoTypeId = output.split("Created Stored InfoType: ")[1].split("\n")[0];
    assertThat(storedInfoTypeId)
        .contains("projects/" + PROJECT_ID + "/locations/global/storedInfoTypes/github-usernames");
    DLP_SERVICE_CLIENT.deleteStoredInfoType(storedInfoTypeId);
  }

  @Test
  public void testUpdateStoredInfoType() throws Exception {
    String infoTypeId = UUID.randomUUID().toString();
    createStoredInfoType(PROJECT_ID, GCS_PATH, infoTypeId);
    UpdateStoredInfoType.updateStoredInfoType(
        PROJECT_ID, GCS_PATH, GCS_PATH, infoTypeId);
    String output = bout.toString();
    assertThat(output).contains("Updated stored InfoType successfully");

    DeleteStoredInfoTypeRequest deleteStoredInfoTypeRequest =
            DeleteStoredInfoTypeRequest.newBuilder()
                    .setName(ProjectStoredInfoTypeName.of(PROJECT_ID, infoTypeId).toString())
                    .build();

    DLP_SERVICE_CLIENT.deleteStoredInfoType(deleteStoredInfoTypeRequest);
  }
}
