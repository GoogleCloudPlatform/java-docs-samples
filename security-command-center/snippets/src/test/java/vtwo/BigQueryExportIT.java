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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

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
import java.io.IOException;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import vtwo.bigquery.CreateBigQueryExport;
import vtwo.bigquery.DeleteBigQueryExport;
import vtwo.bigquery.GetBigQueryExport;
import vtwo.bigquery.ListBigQueryExports;
import vtwo.bigquery.UpdateBigQueryExport;

public class BigQueryExportIT {

  public static final String ORGANIZATION_ID = "test-organization-id";
  public static final String PROJECT_ID = "test-project-id";
  private static final String LOCATION = "test-LOCATION";
  private static final String BQ_DATASET_NAME = "test-dataset-id";
  private static final String BQ_EXPORT_ID = "test-export-id";

  @Test
  public void testCreateBigQueryExport() throws IOException {
    // Mock SecurityCenterClient.
    SecurityCenterClient client = mock(SecurityCenterClient.class);
    try (MockedStatic<SecurityCenterClient> clientMock = Mockito.mockStatic(
        SecurityCenterClient.class)) {
      clientMock.when(SecurityCenterClient::create).thenReturn(client);

      // Define test data.
      String filter = "test-filter";

      // Build the parent of the request.
      OrganizationLocationName organizationName = OrganizationLocationName.of(ORGANIZATION_ID,
          LOCATION);

      // Build the BigQueryExport response.
      BigQueryExport expectedExport = BigQueryExport.newBuilder()
          .setDescription(
              "Export low and medium findings if the compute resource has an IAM anomalous grant")
          .setFilter(filter)
          .setDataset(String.format("projects/%s/datasets/%s", PROJECT_ID, BQ_DATASET_NAME))
          .build();

      // Build the CreateBigQueryExportRequest request.
      CreateBigQueryExportRequest expectedRequest = CreateBigQueryExportRequest.newBuilder()
          .setParent(organizationName.toString())
          .setBigQueryExport(expectedExport)
          .setBigQueryExportId(BQ_EXPORT_ID)
          .build();

      // Mock the createBigQueryExport method to return the expected response.
      Mockito.when(client.createBigQueryExport(any())).thenReturn(expectedExport);

      // Call the createBigQueryExport method.
      CreateBigQueryExport.createBigQueryExport(ORGANIZATION_ID, LOCATION, PROJECT_ID, filter,
          BQ_DATASET_NAME, BQ_EXPORT_ID);

      // Verify that the createBigQueryExport method was called with the expected request.
      Mockito.verify(client).createBigQueryExport(expectedRequest);
    }
  }

  @Test
  public void testDeleteBigQueryExport() throws IOException {
    // Mock the SecurityCenterClient.
    SecurityCenterClient client = mock(SecurityCenterClient.class);
    try (MockedStatic<SecurityCenterClient> clientMock = Mockito.mockStatic(
        SecurityCenterClient.class)) {
      clientMock.when(SecurityCenterClient::create).thenReturn(client);

      // Build the BigQuery export name.
      BigQueryExportName bigQueryExportName = BigQueryExportName.of(ORGANIZATION_ID, LOCATION,
          BQ_EXPORT_ID);

      // Build the delete request.
      DeleteBigQueryExportRequest bigQueryExportRequest = DeleteBigQueryExportRequest.newBuilder()
          .setName(bigQueryExportName.toString())
          .build();

      // Mock the deleteBigQueryExport method to return successfully.
      doNothing().when(client).deleteBigQueryExport(bigQueryExportRequest);

      // Call the deleteBigQueryExport method.
      DeleteBigQueryExport.deleteBigQueryExport(ORGANIZATION_ID, LOCATION, BQ_EXPORT_ID);

      // Verify that the deleteBigQueryExport method was called with the expected request.
      Mockito.verify(client).deleteBigQueryExport(bigQueryExportRequest);
    }
  }

  @Test
  public void testGetBigQueryExport() throws IOException {
    // Mock the SecurityCenterClient.
    SecurityCenterClient client = mock(SecurityCenterClient.class);
    try (MockedStatic<SecurityCenterClient> clientMock = Mockito.mockStatic(
        SecurityCenterClient.class)) {
      clientMock.when(SecurityCenterClient::create).thenReturn(client);

      // Build BigQueryExport response.
      BigQueryExport expectedExport = BigQueryExport.newBuilder()
          .setName(
              String.format("organizations/%s/locations/%s/bigQueryExports/%s", ORGANIZATION_ID,
                  LOCATION, BQ_EXPORT_ID))
          .build();

      // Build the BigQueryExportName and request.
      BigQueryExportName bigQueryExportName = BigQueryExportName.of(ORGANIZATION_ID, LOCATION,
          BQ_EXPORT_ID);

      // Build the GetBigQueryExportRequest request.
      GetBigQueryExportRequest request = GetBigQueryExportRequest.newBuilder()
          .setName(bigQueryExportName.toString())
          .build();

      // Mock the getBigQueryExport method to return the expected response.
      Mockito.when(client.getBigQueryExport(request)).thenReturn(expectedExport);

      // Call the getBigQueryExport method.
      GetBigQueryExport.getBigQueryExport(ORGANIZATION_ID, LOCATION, BQ_EXPORT_ID);

      // Verify that the getBigQueryExport method was called with the expected request.
      Mockito.verify(client).getBigQueryExport(request);
    }
  }

  @Test
  public void testListBigQueryExports() throws IOException {
    // Mock the SecurityCenterClient.
    SecurityCenterClient client = mock(SecurityCenterClient.class);
    try (MockedStatic<SecurityCenterClient> clientMock = Mockito.mockStatic(
        SecurityCenterClient.class)) {
      clientMock.when(SecurityCenterClient::create).thenReturn(client);

      // Define test data.
      String exportId1 = "export-1";
      String exportId2 = "export-2";

      // Build BigQueryExport objects for the response.
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

      // Mock the ListBigQueryExportsPagedResponse.
      ListBigQueryExportsPagedResponse pagedResponse = mock(ListBigQueryExportsPagedResponse.class);
      Mockito.when(pagedResponse.iterateAll()).thenReturn(ImmutableList.of(export1, export2));

      // Mock the client.listBigQueryExports method to return the paged response.
      Mockito.when(client.listBigQueryExports(any(ListBigQueryExportsRequest.class)))
          .thenReturn(pagedResponse);

      // Call the listBigQueryExports method.
      ListBigQueryExports.listBigQueryExports(ORGANIZATION_ID, LOCATION);

      // Verify that the client.listBigQueryExports method was called with the expected request.
      Mockito.verify(client).listBigQueryExports(ListBigQueryExportsRequest.newBuilder()
          .setParent(OrganizationLocationName.of(ORGANIZATION_ID, LOCATION).toString())
          .build());
    }
  }

  @Test
  public void testUpdateBigQueryExport() throws IOException {
    // Mock SecurityCenterClient.
    SecurityCenterClient client = mock(SecurityCenterClient.class);
    try (MockedStatic<SecurityCenterClient> clientMock = Mockito.mockStatic(
        SecurityCenterClient.class)) {
      clientMock.when(SecurityCenterClient::create).thenReturn(client);

      // Define test data.
      String filter = "updated filter";

      // Build the expected BigQueryExport response.
      BigQueryExport expectedExport = BigQueryExport.newBuilder()
          .setName(
              String.format("organizations/%s/locations/%s/bigQueryExports/%s", ORGANIZATION_ID,
                  LOCATION, BQ_EXPORT_ID))
          .setFilter(filter)
          .setDescription("Updated description.")
          .build();

      // Build BigQueryExportName, UpdateMask, and request.
      FieldMask updateMask = FieldMask.newBuilder().addPaths("filter").addPaths("description")
          .build();

      // Build the UpdateBigQueryExportRequest.
      UpdateBigQueryExportRequest request = UpdateBigQueryExportRequest.newBuilder()
          .setBigQueryExport(expectedExport)
          .setUpdateMask(updateMask)
          .build();

      // Mock the updateBigQueryExport method to return the expected response.
      Mockito.when(client.updateBigQueryExport(request)).thenReturn(expectedExport);

      // Call the updateBigQueryExport method.
      UpdateBigQueryExport.updateBigQueryExport(ORGANIZATION_ID, LOCATION, filter, BQ_EXPORT_ID);

      // Verify that the updateBigQueryExport method was called with the expected request.
      Mockito.verify(client).updateBigQueryExport(request);
    }
  }

}