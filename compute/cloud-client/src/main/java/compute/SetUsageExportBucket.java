/*
 * Copyright 2021 Google LLC
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

package compute;

// [START compute_instances_verify_default_value]

import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import com.google.cloud.compute.v1.Project;
import com.google.cloud.compute.v1.ProjectsClient;
import com.google.cloud.compute.v1.SetUsageExportBucketProjectRequest;
import com.google.cloud.compute.v1.UsageExportLocation;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SetUsageExportBucket {

  public static void main(String[] args) throws IOException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // TODO(developer): Create a Google Cloud Storage bucket.
    // bucketName: to run this snippet, an existing Google Cloud Storage bucket is required.
    String project = "your-project-id";
    String bucketName = "your-bucket-name";
    setUsageExportBucket(project, bucketName);
  }

  // Check whether an optional param was set to the default value
  // when not provided an explicit value.
  public static void setUsageExportBucket(String project, String bucketName)
      throws IOException, InterruptedException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "projectsClient.close()" method on the client to safely
    // clean up any remaining background resources.
    try (ProjectsClient projectsClient = ProjectsClient.create()) {
      // Enable usage statistics for the project and set the Cloud Storage bucket name
      // where logs should be stored.
      // Here we explicitly DO NOT set ReportName.
      Operation response = projectsClient
          .setUsageExportBucket(SetUsageExportBucketProjectRequest.newBuilder().setProject(project)
              .setUsageExportLocationResource(
                  UsageExportLocation.newBuilder().setBucketName(bucketName).build()).build());

      // Wait for 5 seconds to activate the export settings.
      if (response.getStatus() != Status.DONE) {
        TimeUnit.SECONDS.sleep(5);
      }

      // Get the setting from server.
      Project projectResponse = projectsClient.get(project);

      // Construct proper values to be displayed taking into account default values behaviour.
      if (projectResponse.hasUsageExportLocation()) {
        UsageExportLocation usageExportLocation = projectResponse.getUsageExportLocation();
        bucketName = usageExportLocation.getBucketName();
        String reportNamePrefix = "";

        // We verify that the server explicitly sent the optional field.
        if (usageExportLocation.hasReportNamePrefix()) {
          reportNamePrefix = usageExportLocation.getReportNamePrefix();

          if (reportNamePrefix.isEmpty()) {
            // Although the server explicitly sent the empty string value,
            // the next usage report generated with these settings still has the default
            // prefix value "usage".
            // (ref: https://cloud.google.com/compute/docs/reference/rest/v1/projects/get)
            reportNamePrefix = "usage";
          }
        }
        System.out.println(String.format("Usage export bucket for project %s set.", project));
        System.out.println(
            String.format("Bucket: %s, Report name prefix: %s", bucketName, reportNamePrefix));
      }
    }
  }
}
// [END compute_instances_verify_default_value]
