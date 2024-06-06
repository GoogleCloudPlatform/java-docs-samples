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

// [START securitycenter_list_bigquery_export_v2]

import com.google.cloud.securitycenter.v2.BigQueryExport;
import com.google.cloud.securitycenter.v2.ListBigQueryExportsRequest;
import com.google.cloud.securitycenter.v2.OrganizationLocationName;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.SecurityCenterClient.ListBigQueryExportsPagedResponse;
import java.io.IOException;

public class ListBigQueryExports {

  public static void main(String[] args) throws IOException {
    // TODO(Developer): Modify the following variable values.
    // organizationId: Google Cloud Organization id.
    String organizationId = "{google-cloud-organization-id}";

    // Specify the location to list the findings.
    String location = "global";

    listBigQueryExports(organizationId, location);
  }

  // List BigQuery exports in the given parent.
  public static ListBigQueryExportsPagedResponse listBigQueryExports(String organizationId,
      String location) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {
      OrganizationLocationName organizationName = OrganizationLocationName.of(organizationId,
          location);

      ListBigQueryExportsRequest request = ListBigQueryExportsRequest.newBuilder()
          .setParent(organizationName.toString())
          .build();

      ListBigQueryExportsPagedResponse response = client.listBigQueryExports(request);

      System.out.println("Listing BigQuery exports:");
      for (BigQueryExport bigQueryExport : response.iterateAll()) {
        System.out.println(bigQueryExport.getName());
      }
      return response;
    }
  }
}
// [END securitycenter_list_bigquery_export_v2]