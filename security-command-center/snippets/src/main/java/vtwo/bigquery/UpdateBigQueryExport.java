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

// [START securitycenter_update_bigquery_export_v2]

import com.google.cloud.securitycenter.v2.BigQueryExport;
import com.google.cloud.securitycenter.v2.BigQueryExportName;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.UpdateBigQueryExportRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;

public class UpdateBigQueryExport {

  public static void main(String[] args) throws IOException {
    // TODO(Developer): Modify the following variable values.
    // organizationId: Google Cloud Organization id.
    String organizationId = "{google-cloud-organization-id}";

    // Specify the location to list the findings.
    String location = "global";

    // filter: Expression that defines the filter to apply across create/update events of findings.
    String filter =
        "severity=\"LOW\" OR severity=\"MEDIUM\" AND "
            + "category=\"Persistence: IAM Anomalous Grant\" AND "
            + "-resource.type:\"compute\"";

    // bigQueryExportId: Unique identifier provided by the client.
    // For more info, see:
    // https://cloud.google.com/security-command-center/docs/how-to-analyze-findings-in-big-query#export_findings_from_to
    String bigQueryExportId = "{bigquery-export-id}";

    updateBigQueryExport(organizationId, location, filter, bigQueryExportId);
  }

  // Updates an existing BigQuery export.
  public static BigQueryExport updateBigQueryExport(String organizationId, String location,
      String filter, String bigQueryExportId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {
      // Optionally BigQueryExportName or String can be used
      // String bigQueryExportName = String.format("organizations/%s/locations/%s
      // /bigQueryExports/%s",organizationId,location, bigQueryExportId);
      BigQueryExportName bigQueryExportName = BigQueryExportName.of(organizationId, location,
          bigQueryExportId);

      //  Set the new values for export configuration.
      BigQueryExport bigQueryExport =
          BigQueryExport.newBuilder()
              .setName(bigQueryExportName.toString())
              .setDescription("Updated description.")
              .setFilter(filter)
              .build();

      UpdateBigQueryExportRequest request =
          UpdateBigQueryExportRequest.newBuilder()
              .setBigQueryExport(bigQueryExport)
              // Set the update mask to specify which properties should be updated.
              // If empty, all mutable fields will be updated.
              // For more info on constructing field mask path, see the proto or:
              // https://cloud.google.com/java/docs/reference/protobuf/latest/com.google.protobuf.FieldMask
              .setUpdateMask(FieldMask.newBuilder()
                  .addPaths("filter")
                  .addPaths("description").build())
              .build();

      BigQueryExport response = client.updateBigQueryExport(request);
      System.out.println("BigQueryExport updated successfully!");
      return response;
    }
  }
}
// [END securitycenter_update_bigquery_export_v2]