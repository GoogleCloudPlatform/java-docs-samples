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

public class DefaultValue {

  public static void main(String[] args) throws IOException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // TODO(developer): Create a Google Cloud Storage bucket.
    // bucketName: to run this snippet, an existing Google Cloud Storage bucket is required.
    String project = "your-project-id";
    String bucketName = "your-bucket-name";
    defaultValues(project, bucketName);
  }

  // Check whether an optional param was set to the default value when not provided an explicit value.
  public static void defaultValues(String project, String bucketName)
      throws IOException, InterruptedException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "projectsClient.close()" method on the client to safely clean up any remaining background resources.
    try (ProjectsClient projectsClient = ProjectsClient.create()) {
      // Enable usage statistics for the project and set the Cloud Storage bucket name where logs should be stored.
      // Here we explicitly DO NOT set ReportName.
      Operation response = projectsClient
          .setUsageExportBucket(SetUsageExportBucketProjectRequest.newBuilder().setProject(project)
              .setUsageExportLocationResource(
                  UsageExportLocation.newBuilder().setBucketName(bucketName).build()).build());

      // Wait for 5 seconds to activate the export settings.
      if (response.getStatus() != Status.DONE) {
        TimeUnit.SECONDS.sleep(5);
      }

      // Even though only bucketName was set, the ReportName was set to the default value "usage" by the server.
      // This is confirmed by hasReportNamePrefix() which evaluates to true.
      // When the user doesn't set the value, getReportNamePrefix() returns an empty value.
      Project projectResponse = projectsClient.get(project);
      System.out
          .println("Usage Export location available: " + projectResponse.hasUsageExportLocation());
      System.out.println("Report Name Prefix available: " + projectResponse.getUsageExportLocation()
          .hasReportNamePrefix());
      System.out.println(
          "Report Name Prefix: " + projectResponse.getUsageExportLocation().getReportNamePrefix());
    }
  }
}
// [END compute_instances_verify_default_value]
