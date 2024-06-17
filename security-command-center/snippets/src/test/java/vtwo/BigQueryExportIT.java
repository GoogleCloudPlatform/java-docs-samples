/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vtwo;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.securitycenter.v2.BigQueryExport;
import com.google.cloud.securitycenter.v2.BigQueryExportName;
import com.google.cloud.securitycenter.v2.CreateBigQueryExportRequest;
import com.google.cloud.securitycenter.v2.DeleteBigQueryExportRequest;
import com.google.cloud.securitycenter.v2.GetBigQueryExportRequest;
import com.google.cloud.securitycenter.v2.ListBigQueryExportsRequest;
import com.google.cloud.securitycenter.v2.OrganizationLocationName;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.SecurityCenterClient.ListBigQueryExportsPagedResponse;
import com.google.cloud.securitycenter.v2.UpdateBigQueryExportRequest;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.FieldMask;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import vtwo.bigquery.CreateBigQueryExport;
import vtwo.bigquery.DeleteBigQueryExport;
import vtwo.bigquery.GetBigQueryExport;
import vtwo.bigquery.ListBigQueryExports;
import vtwo.bigquery.UpdateBigQueryExport;

public class BigQueryExportIT {

  private static final String ORGANIZATION_ID = "test-organization-id";
  private static final String PROJECT_ID = "test-project-id";
  private static final String LOCATION = "global";
  private static final String BQ_DATASET_NAME = "test-dataset-id";
  private static final String BQ_EXPORT_ID = "test-export-id";
  private static ByteArrayOutputStream stdOut;

  @BeforeClass
  public static void setUp() {
    final PrintStream out = System.out;
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    stdOut = null;
    System.setOut(out);
  }

  @AfterClass
  public static void cleanUp() {
    stdOut = null;
    System.setOut(null);
  }

  @Before
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @Test
  public void testCreateBigQueryExport() throws IOException {
    // Mocking and test data setup.
    SecurityCenterClient client = mock(SecurityCenterClient.class);
    try (MockedStatic<SecurityCenterClient> clientMock = Mockito.mockStatic(
        SecurityCenterClient.class)) {
      clientMock.when(SecurityCenterClient::create).thenReturn(client);
      // Mocking and test data setup.
      String filter = "test-filter";
      OrganizationLocationName organizationName = OrganizationLocationName.of(ORGANIZATION_ID,
          LOCATION);
      // Building expected BigQueryExport.
      BigQueryExport expectedExport = BigQueryExport.newBuilder()
          .setDescription(
              "Export low and medium findings if the compute resource has an IAM anomalous grant")
          .setFilter(filter)
          .setDataset(String.format("projects/%s/datasets/%s", PROJECT_ID, BQ_DATASET_NAME))
          .build();
      // Building CreateBigQueryExportRequest.
      CreateBigQueryExportRequest request = CreateBigQueryExportRequest.newBuilder()
          .setParent(organizationName.toString())
          .setBigQueryExport(expectedExport)
          .setBigQueryExportId(BQ_EXPORT_ID)
          .build();
      // Mocking createBigQueryExport.
      when(client.createBigQueryExport(request)).thenReturn(expectedExport);

      // Calling createBigQueryExport.
      BigQueryExport response = CreateBigQueryExport.createBigQueryExport(ORGANIZATION_ID, LOCATION,
          PROJECT_ID, filter,
          BQ_DATASET_NAME, BQ_EXPORT_ID);
      // Verifying createBigQueryExport was called.
      verify(client).createBigQueryExport(request);

      // Asserts the created BigQueryExport matches the expected request.
      assertThat(response).isEqualTo(expectedExport);
    }
  }

  @Test
  public void testDeleteBigQueryExport() throws IOException {
    // Mocking and test data setup.
    SecurityCenterClient client = mock(SecurityCenterClient.class);
    try (MockedStatic<SecurityCenterClient> clientMock = Mockito.mockStatic(
        SecurityCenterClient.class)) {
      clientMock.when(SecurityCenterClient::create).thenReturn(client);
      // Building BigQueryExportName.
      BigQueryExportName bigQueryExportName = BigQueryExportName.of(ORGANIZATION_ID, LOCATION,
          BQ_EXPORT_ID);
      // Building DeleteBigQueryExportRequest.
      DeleteBigQueryExportRequest request = DeleteBigQueryExportRequest.newBuilder()
          .setName(bigQueryExportName.toString())
          .build();
      // Mocking deleteBigQueryExport.
      doNothing().when(client).deleteBigQueryExport(request);

      // Calling deleteBigQueryExport.
      DeleteBigQueryExport.deleteBigQueryExport(ORGANIZATION_ID, LOCATION, BQ_EXPORT_ID);

      // Verifying deleteBigQueryExport was called.
      verify(client).deleteBigQueryExport(request);
    }
  }

  @Test
  public void testGetBigQueryExport() throws IOException {
    // Mocking and test data setup.
    SecurityCenterClient client = mock(SecurityCenterClient.class);
    try (MockedStatic<SecurityCenterClient> clientMock = Mockito.mockStatic(
        SecurityCenterClient.class)) {
      clientMock.when(SecurityCenterClient::create).thenReturn(client);
      // Building Expected BigQueryExport.
      BigQueryExport expectedExport = BigQueryExport.newBuilder()
          .setName(
              String.format("organizations/%s/locations/%s/bigQueryExports/%s", ORGANIZATION_ID,
                  LOCATION, BQ_EXPORT_ID))
          .build();
      // Build the BigQueryExportName and request.
      BigQueryExportName bigQueryExportName = BigQueryExportName.of(ORGANIZATION_ID, LOCATION,
          BQ_EXPORT_ID);
      GetBigQueryExportRequest request = GetBigQueryExportRequest.newBuilder()
          .setName(bigQueryExportName.toString())
          .build();
      // Mocking getBigQueryExport.
      when(client.getBigQueryExport(request)).thenReturn(expectedExport);

      // Calling getBigQueryExport.
      BigQueryExport response = GetBigQueryExport.getBigQueryExport(ORGANIZATION_ID, LOCATION,
          BQ_EXPORT_ID);
      // Verifying getBigQueryExport was called.
      verify(client).getBigQueryExport(request);

      // Verifies the retrieved BigQueryExport matches the expected export.
      assertThat(response).isEqualTo(expectedExport);
    }
  }

  @Test
  public void testListBigQueryExports() throws IOException {
    // Mocking and test data setup.
    SecurityCenterClient client = mock(SecurityCenterClient.class);
    try (MockedStatic<SecurityCenterClient> clientMock = Mockito.mockStatic(
        SecurityCenterClient.class)) {
      clientMock.when(SecurityCenterClient::create).thenReturn(client);
      String exportId1 = "export-1";
      String exportId2 = "export-2";
      // Building Expected BigQueryExports.
      BigQueryExport export1 = BigQueryExport.newBuilder()
          .setName(
              String.format("organizations/%s/locations/%s/bigQueryExports/%s", ORGANIZATION_ID,
                  LOCATION, exportId1))
          .build();
      BigQueryExport export2 = BigQueryExport.newBuilder()
          .setName(
              String.format("organizations/%s/locations/%s/bigQueryExports/%s", ORGANIZATION_ID,
                  LOCATION, exportId2))
          .build();
      // Mocking ListBigQueryExportsPagedResponse.
      ListBigQueryExportsPagedResponse pagedResponse = mock(ListBigQueryExportsPagedResponse.class);
      when(pagedResponse.iterateAll()).thenReturn(ImmutableList.of(export1, export2));
      // Building ListBigQueryExportsRequest.
      ListBigQueryExportsRequest request = ListBigQueryExportsRequest.newBuilder()
          .setParent(OrganizationLocationName.of(ORGANIZATION_ID, LOCATION).toString())
          .build();
      // Mock the client.listBigQueryExports method to return the paged response.
      when(client.listBigQueryExports(request)).thenReturn(pagedResponse);

      // Calling listBigQueryExports.
      ListBigQueryExportsPagedResponse response = ListBigQueryExports.listBigQueryExports(
          ORGANIZATION_ID, LOCATION);
      // Verifying client.listBigQueryExports was called.
      verify(client).listBigQueryExports(request);

      // Ensures the response from listBigQueryExports matches the mocked paged response.
      assertThat(response).isEqualTo(pagedResponse);
    }
  }

  @Test
  public void testUpdateBigQueryExport() throws IOException {
    // Mocking and test data setup.
    SecurityCenterClient client = mock(SecurityCenterClient.class);
    try (MockedStatic<SecurityCenterClient> clientMock = Mockito.mockStatic(
        SecurityCenterClient.class)) {
      clientMock.when(SecurityCenterClient::create).thenReturn(client);
      String filter = "updated filter";
      String name = String.format("organizations/%s/locations/%s/bigQueryExports/%s",
          ORGANIZATION_ID,
          LOCATION, BQ_EXPORT_ID);
      // Building expected BigQueryExport.
      BigQueryExport expectedExport = BigQueryExport.newBuilder()
          .setName(name)
          .setFilter(filter)
          .setDescription("Updated description.")
          .build();
      // Building Update Parameters.
      FieldMask updateMask = FieldMask.newBuilder().addPaths("filter").addPaths("description")
          .build();
      UpdateBigQueryExportRequest request = UpdateBigQueryExportRequest.newBuilder()
          .setBigQueryExport(expectedExport)
          .setUpdateMask(updateMask)
          .build();
      // Mocking updateBigQueryExport.
      when(client.updateBigQueryExport(request)).thenReturn(expectedExport);

      // Calling updateBigQueryExport.
      BigQueryExport response = UpdateBigQueryExport.updateBigQueryExport(ORGANIZATION_ID, LOCATION,
          filter, BQ_EXPORT_ID);
      // Verifying updateBigQueryExport was called.
      verify(client).updateBigQueryExport(request);

      // Ensures the updated BigQuery Export name matches the expected name.
      assertThat(response.getName()).isEqualTo(name);
    }
  }

} 