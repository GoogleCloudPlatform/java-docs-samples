/*
 Copyright 2015, Google, Inc.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
package com.google.cloud.bigquery.samples;

import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.model.Job;
import com.google.api.services.bigquery.model.JobConfiguration;
import com.google.api.services.bigquery.model.JobConfigurationExtract;
import com.google.api.services.bigquery.model.TableReference;

import java.io.IOException;
import java.util.Scanner;

/**
 * Sample of how to Export Cloud Data.
 */
public class ExportDataCloudStorageSample {
  /**
   * Protected constructor since this is a collection of static functions.
   */
  protected ExportDataCloudStorageSample() {
    super();
  }

  /**
   * This program can be run to demonstrate running a Bigquery query from the
   * CLI.
   * @param args Command line args
   * @throws IOException If there is an error connceting to bigquery
   * @throws InterruptedException Should never be thrown.
   */
  // [START main]
  public static void main(final String[] args) throws IOException, InterruptedException {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Enter your project id: ");
    String projectId = scanner.nextLine();
    System.out.println("Enter your dataset id: ");
    String datasetId = scanner.nextLine();
    System.out.println("Enter your table id: ");
    String tableId = scanner.nextLine();
    System.out.println("Enter the Google Cloud Storage Path to which you'd " + "like to export: ");
    String cloudStoragePath = scanner.nextLine();
    System.out.println("Enter how often to check if your job is  complete " + "(milliseconds): ");
    long interval = scanner.nextLong();
    scanner.close();

    run(cloudStoragePath, projectId, datasetId, tableId, interval);
  }
  // [END main]

  /**
   * Run the bigquery ClI.
   * @param cloudStoragePath The bucket we are using
   * @param projectId Project id
   * @param datasetId datasetid
   * @param tableId tableid
   * @param interval interval to wait between polling in milliseconds
   * @throws IOException Thrown if there is an error connecting to Bigquery.
   * @throws InterruptedException Should never be thrown
   */
  // [START run]
  public static void run(
      final String cloudStoragePath,
      final String projectId,
      final String datasetId,
      final String tableId,
      final long interval)
      throws IOException, InterruptedException {

    Bigquery bigquery = BigQueryServiceFactory.getService();

    Job extractJob =
        extractJob(
            bigquery,
            cloudStoragePath,
            new TableReference()
                .setProjectId(projectId)
                .setDatasetId(datasetId)
                .setTableId(tableId));

    Bigquery.Jobs.Get getJob =
        bigquery
            .jobs()
            .get(
                extractJob.getJobReference().getProjectId(),
                extractJob.getJobReference().getJobId());

    BigQueryUtils.pollJob(getJob, interval);

    System.out.println("Export is Done!");
  }
  // [END run]

  /**
   * A job that extracts data from a table.
   * @param bigquery Bigquery service to use
   * @param cloudStoragePath Cloud storage bucket we are inserting into
   * @param table Table to extract from
   * @return The job to extract data from the table
   * @throws IOException Thrown if error connceting to Bigtable
   */
  // [START extract_job]
  public static Job extractJob(
      final Bigquery bigquery, final String cloudStoragePath, final TableReference table)
      throws IOException {

    JobConfigurationExtract extract =
        new JobConfigurationExtract().setSourceTable(table).setDestinationUri(cloudStoragePath);

    return bigquery
        .jobs()
        .insert(
            table.getProjectId(),
            new Job().setConfiguration(new JobConfiguration().setExtract(extract)))
        .execute();
  }
  // [END extract_job]
}
