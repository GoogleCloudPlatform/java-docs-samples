/*
 * Copyright 2019 Google LLC
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

package com.example.bigquery;

// [START bigquery_create_dataset]
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetInfo;

public class RunCreateDataset {

  public static void runCreateDataset() {
    // TODO(developer): Replace these variables before running the sample.
    String datasetName = "my-dataset-name";
    createDataset(datasetName);
  }

  public static Dataset createDataset(String datasetName) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

    DatasetInfo datasetInfo = DatasetInfo.newBuilder(datasetName).build();
    return bigquery.create(datasetInfo);
  }
}
// [END bigquery_create_dataset]