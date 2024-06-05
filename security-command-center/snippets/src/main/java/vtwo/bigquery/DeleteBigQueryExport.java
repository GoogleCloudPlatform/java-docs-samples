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

package vtwo.bigquery;

// [START securitycenter_delete_bigquery_export_v2]

import com.google.cloud.securitycenter.v2.BigQueryExportName;
import com.google.cloud.securitycenter.v2.DeleteBigQueryExportRequest;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import java.io.IOException;

public class DeleteBigQueryExport {

  public static void main(String[] args) throws IOException {
    // TODO(Developer): Modify the following variable values.
    // organizationId: Google Cloud Organization id.
    String organizationId = "{google-cloud-organization-id}";

    // Specify the location to list the findings.
    String location = "global";

    // bigQueryExportId: Unique identifier that is used to identify the export.
    String bigQueryExportId = "{bigquery-export-id}";

    deleteBigQueryExport(organizationId, location, bigQueryExportId);
  }

  // Delete an existing BigQuery export.
  public static void deleteBigQueryExport(String organizationId, String location,
      String bigQueryExportId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {
      // Optionally BigQueryExportName or String can be used
      // String bigQueryExportName = String.format("organizations/%s/locations/%s
      // /bigQueryExports/%s",organizationId,location, bigQueryExportId);
      BigQueryExportName bigQueryExportName = BigQueryExportName.of(organizationId, location,
          bigQueryExportId);

      DeleteBigQueryExportRequest bigQueryExportRequest =
          DeleteBigQueryExportRequest.newBuilder()
              .setName(bigQueryExportName.toString())
              .build();

      client.deleteBigQueryExport(bigQueryExportRequest);
      System.out.printf("BigQuery export request deleted successfully: %s", bigQueryExportId);
    }
  }
}
// [END securitycenter_delete_bigquery_export_v2]