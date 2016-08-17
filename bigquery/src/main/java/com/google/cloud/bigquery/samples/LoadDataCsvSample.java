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
import com.google.api.services.bigquery.model.JobConfigurationLoad;
import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableSchema;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Scanner;

/**
 * Cli tool to load data from a CSV into Bigquery.
 */
public class LoadDataCsvSample {

  /**
   * Protected constructor since this is a collection of static methods.
   */
  protected LoadDataCsvSample() {}

  /**
   * Cli tool to load data from a CSV into Bigquery.
   * @param args Command line args, should be empty
   * @throws IOException IOException
   * @throws InterruptedException InterruptedException
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
    System.out.println("Enter the Google Cloud Storage Path to the data " + "you'd like to load: ");
    String cloudStoragePath = scanner.nextLine();
    System.out.println("Enter the filepath to your schema: ");
    String sourceSchemaPath = scanner.nextLine();

    System.out.println("Enter how often to check if your job is complete " + "(milliseconds): ");
    long interval = scanner.nextLong();
    scanner.close();

    run(
        cloudStoragePath,
        projectId,
        datasetId,
        tableId,
        new FileReader(new File(sourceSchemaPath)),
        interval);
  }
  // [END main]

  /**
   * Run the bigquery ClI.
   * @param cloudStoragePath The bucket we are using
   * @param projectId Project id
   * @param datasetId datasetid
   * @param tableId tableid
   * @param schemaSource Source of the schema
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
      final Reader schemaSource,
      final long interval)
      throws IOException, InterruptedException {

    Bigquery bigquery = BigQueryServiceFactory.getService();

    Job loadJob =
        loadJob(
            bigquery,
            cloudStoragePath,
            new TableReference()
                .setProjectId(projectId)
                .setDatasetId(datasetId)
                .setTableId(tableId),
            BigQueryUtils.loadSchema(schemaSource));

    Bigquery.Jobs.Get getJob =
        bigquery
            .jobs()
            .get(loadJob.getJobReference().getProjectId(), loadJob.getJobReference().getJobId());

    BigQueryUtils.pollJob(getJob, interval);

    System.out.println("Load is Done!");
  }
  // [END run]

  /**
   * A job that extracts data from a table.
   * @param bigquery Bigquery service to use
   * @param cloudStoragePath Cloud storage bucket we are inserting into
   * @param table Table to extract from
   * @param schema The schema of the table we are loading into
   * @return The job to extract data from the table
   * @throws IOException Thrown if error connceting to Bigtable
   */
  // [START load_job]
  public static Job loadJob(
      final Bigquery bigquery,
      final String cloudStoragePath,
      final TableReference table,
      final TableSchema schema)
      throws IOException {

    JobConfigurationLoad load =
        new JobConfigurationLoad()
            .setDestinationTable(table)
            .setSchema(schema)
            .setSourceUris(Collections.singletonList(cloudStoragePath));

    return bigquery
        .jobs()
        .insert(
            table.getProjectId(), new Job().setConfiguration(new JobConfiguration().setLoad(load)))
        .execute();
  }
  // [END load_job]
}
