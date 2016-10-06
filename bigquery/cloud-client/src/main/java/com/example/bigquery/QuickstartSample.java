/*
  Copyright 2016, Google, Inc.

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

package com.example.bigquery;

// [START bigquery_quickstart]
// Imports the Google Cloud client library
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetInfo;

public class QuickstartSample {
  public static void main(String... args) throws Exception {
    // Instantiates a client
    BigQuery bigquery = BigQueryOptions.defaultInstance().service();
    // The name for the new dataset
    String datasetName = "my_new_dataset";
    Dataset dataset = null;
    DatasetInfo datasetInfo = DatasetInfo.builder(datasetName).build();
    // Creates the new dataset
    dataset = bigquery.create(datasetInfo);
    System.out.printf("Dataset %s created.%n", dataset.datasetId().dataset());
  }
}
// [END bigquery_quickstart]
