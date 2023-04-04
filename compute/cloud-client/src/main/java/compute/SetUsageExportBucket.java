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

/* A sample script showing how to handle default values when communicating
   with the Compute Engine API. */

// [START compute_usage_report_set]
// [START compute_usage_report_get]
// [START compute_usage_report_disable]

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Project;
import com.google.cloud.compute.v1.ProjectsClient;
import com.google.cloud.compute.v1.SetUsageExportBucketProjectRequest;
import com.google.cloud.compute.v1.UsageExportLocation;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// [END compute_usage_report_disable]
// [END compute_usage_report_get]
// [END compute_usage_report_set]

public class SetUsageExportBucket {

  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // TODO(developer): Create a Google Cloud Storage bucket.
    // bucketName: Cloud Storage Bucket used to store Compute Engine usage reports.
    // An existing Google Cloud Storage bucket is required.
    String project = "your-project-id";
    String bucketName = "your-bucket-name";
    String reportNamePrefix = "custom-report-name";
    setUsageExportBucket(project, bucketName, reportNamePrefix);
    getUsageExportBucket(project);
    disableUsageExportBucket(project);
  }

  // [START compute_usage_report_set]

  // Set Compute Engine usage export bucket for the Cloud project.
  // This sample presents how to interpret the default value for the report name prefix parameter.
  public static void setUsageExportBucket(String project, String bucketName,
      String reportNamePrefix)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {

    // bucketName: Cloud Storage Bucket used to store Compute Engine usage reports.
    // An existing Google Cloud Storage bucket is required.
    // reportNamePrefix: Prefix of the name of the usage report that would
    // store Google Compute Engine data.
    try (ProjectsClient projectsClient = ProjectsClient.create()) {

      // Initialize UsageExportLocation object with provided bucket name and report name prefix.
      UsageExportLocation usageExportLocation = UsageExportLocation.newBuilder()
          .setBucketName(bucketName)
          .setReportNamePrefix(reportNamePrefix).build();

      if (reportNamePrefix.length() == 0) {
        // Sending an empty value for reportNamePrefix results in the
        // next usage report being generated with the default prefix value "usage_gce".
        // (see,
        // https://cloud.google.com/compute/docs/reference/rest/v1/projects/setUsageExportBucket)
        System.out.println("Setting reportNamePrefix to empty value causes the "
            + "report to have the default value of `usage_gce`.");
      }

      // Set the usage export location.
      OperationFuture<Operation, Operation> operation = projectsClient
          .setUsageExportBucketAsync(SetUsageExportBucketProjectRequest.newBuilder()
              .setProject(project)
              .setUsageExportLocationResource(usageExportLocation)
              .build());

      // Wait for the operation to complete.
      Operation response = operation.get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Setting usage export bucket failed ! ! " + response);
        return;
      }
      System.out.println("Operation Status: " + response.getStatus());
    }
  }
  // [END compute_usage_report_set]

  // [START compute_usage_report_get]

  // Retrieve Compute Engine usage export bucket for the Cloud project.
  // Replaces the empty value returned by the API with the default value used
  // to generate report file names.
  public static UsageExportLocation getUsageExportBucket(String project) throws IOException {

    try (ProjectsClient projectsClient = ProjectsClient.create()) {
      // Get the usage export location for the project from the server.
      Project projectResponse = projectsClient.get(project);

      // Replace the empty value returned by the API with the default value
      // used to generate report file names.
      if (projectResponse.hasUsageExportLocation()) {
        UsageExportLocation usageExportLocation = projectResponse.getUsageExportLocation();

        // Verify that the server explicitly sent the optional field.
        if (usageExportLocation.hasReportNamePrefix()) {
          String reportNamePrefix = usageExportLocation.getReportNamePrefix();

          if (reportNamePrefix.length() == 0) {
            // Although the server explicitly sent the empty string value,
            // the next usage report generated with these settings still has the default
            // prefix value "usage_gce".
            // (see, https://cloud.google.com/compute/docs/reference/rest/v1/projects/get)
            reportNamePrefix = "usage_gce";
            System.out.println(
                "Report name prefix not set, replacing with default value of `usage_gce`.");
          }
        }
        return usageExportLocation;
      } else {
        // The usage reports are disabled.
        return null;
      }
    }
  }
  // [END compute_usage_report_get]

  // [START compute_usage_report_disable]

  // Disable Compute Engine usage export bucket for the Cloud project.
  public static boolean disableUsageExportBucket(String project)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {

    try (ProjectsClient projectsClient = ProjectsClient.create()) {

      // Initialize UsageExportLocation object with empty builder to disable usage reports.
      UsageExportLocation usageExportLocation = UsageExportLocation.newBuilder().build();

      // Disable the usage export location.
      OperationFuture<Operation, Operation> operation = projectsClient
          .setUsageExportBucketAsync(SetUsageExportBucketProjectRequest.newBuilder()
              .setProject(project)
              .setUsageExportLocationResource(usageExportLocation)
              .build());

      // Wait for the operation to complete.
      Operation response = operation.get(3, TimeUnit.MINUTES);
      ;

      if (response.hasError()) {
        System.out.println("Disable usage export bucket failed ! ! " + response);
        return true;
      }

      // Wait for the settings to be effected.
      TimeUnit.SECONDS.sleep(15);
      // Return false if the usage reports is disabled.
      return projectsClient.get(project).getUsageExportLocation().hasBucketName();
    }
  }
  // [END compute_usage_report_disable]

}
