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

// [START all]
// [START create_client]
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.QueryResponse;
import com.google.cloud.bigquery.QueryResult;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
// [END create_client]

public class SimpleApp {
  public static void main(String... args) throws Exception {
    // [START create_client]
    BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
    // [END create_client]
    // [START run_query]
    QueryJobConfiguration queryConfig =
        QueryJobConfiguration.newBuilder(
            "SELECT "
                + "APPROX_TOP_COUNT(corpus, 10) as title, "
                + "COUNT(*) as unique_words "
                + "FROM `publicdata.samples.shakespeare`;")
            // Use standard SQL syntax for queries.
            // See: https://cloud.google.com/bigquery/sql-reference/
            .setUseLegacySql(false)
            .build();

    // Create a job ID so that we can safely retry.
    JobId jobId = JobId.of(UUID.randomUUID().toString());
    Job queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());

    // Wait for the query to complete.
    queryJob = queryJob.waitFor();

    // Check for errors
    if (queryJob == null) {
      throw new RuntimeException("Job no longer exists");
    } else if (queryJob.getStatus().getError() != null) {
      // You can also look at queryJob.getStatus().getExecutionErrors() for all
      // errors, not just the latest one.
      throw new RuntimeException(queryJob.getStatus().getError().toString());
    }

    // Get the results.
    QueryResponse response = bigquery.getQueryResults(jobId);
    // [END run_query]

    // [START print_results]
    QueryResult result = response.getResult();

    while (result != null) {
      Iterator<List<FieldValue>> iter = result.iterateAll();

      while (iter.hasNext()) {
        List<FieldValue> row = iter.next();
        List<FieldValue> titles = row.get(0).getRepeatedValue();
        System.out.println("titles:");

        for (FieldValue titleValue : titles) {
          List<FieldValue> titleRecord = titleValue.getRecordValue();
          String title = titleRecord.get(0).getStringValue();
          long uniqueWords = titleRecord.get(1).getLongValue();
          System.out.printf("\t%s: %d\n", title, uniqueWords);
        }

        long uniqueWords = row.get(1).getLongValue();
        System.out.printf("total unique words: %d\n", uniqueWords);
      }

      result = result.getNextPage();
    }
    // [END print_results]
  }
}
// [END all]

