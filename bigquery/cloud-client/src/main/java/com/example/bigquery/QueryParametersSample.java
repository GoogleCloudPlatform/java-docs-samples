/*
  Copyright 2016 Google Inc.

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

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.QueryParameterValue;
import com.google.cloud.bigquery.QueryRequest;
import com.google.cloud.bigquery.QueryResponse;
import com.google.cloud.bigquery.QueryResult;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * A sample that demonstrates use of query parameters.
 */
public class QueryParametersSample {
  private static final int ERROR_CODE = 1;

  private static void printUsage() {
    System.err.println("Usage:");
    System.err.printf(
        "\tmvn exec:java -Dexec.mainClass=%s -Dexec.args='%s'\n",
        QueryParametersSample.class.getCanonicalName(),
        "${sample}");
    System.err.println();
    System.err.println("${sample} can be one of: named, array, timestamp");
    System.err.println();
    System.err.println("Usage for ${sample}=named:");
    System.err.printf(
        "\tmvn exec:java -Dexec.mainClass=%s -Dexec.args='%s'\n",
        QueryParametersSample.class.getCanonicalName(),
        "named ${corpus} ${minWordCount}");
    System.err.println();
    System.err.println("Usage for sample=array:");
    System.err.printf(
        "\tmvn exec:java -Dexec.mainClass=%s -Dexec.args='%s'\n",
        QueryParametersSample.class.getCanonicalName(),
        "array ${gender} ${states...}");
    System.err.println();
    System.err.println("\twhere ${gender} can be on of: M, F");
    System.err.println(
        "\tand ${states} is any upper-case 2-letter code for U.S. a state, e.g. CA.");
    System.err.println();
    System.err.printf(
        "\t\tmvn exec:java -Dexec.mainClass=%s -Dexec.args='%s'\n",
        QueryParametersSample.class.getCanonicalName(),
        "array F MD WA");
  }

  /**
   * Prompts the user for the required parameters to perform a query.
   */
  public static void main(final String[] args) throws IOException, InterruptedException {
    if (args.length < 1) {
      System.err.println("Expected first argument 'sample'");
      printUsage();
      System.exit(ERROR_CODE);
    }
    String sample = args[0];

    switch (sample) {
      case "named":
        if (args.length != 3) {
          System.err.println("Unexpected number of arguments for named query sample.");
          printUsage();
          System.exit(ERROR_CODE);
        }
        runNamed(args[1], Long.parseLong(args[2]));
        break;
      case "array":
        if (args.length < 2) {
          System.err.println("Unexpected number of arguments for array query sample.");
          printUsage();
          System.exit(ERROR_CODE);
        }
        String gender = args[1];
        String[] states = Arrays.copyOfRange(args, 2, args.length);
        runArray(gender, states);
        break;
      case "timestamp":
        if (args.length != 1) {
          System.err.println("Unexpected number of arguments for timestamp query sample.");
          printUsage();
          System.exit(ERROR_CODE);
        }
        runTimestamp();
        break;
      default:
        System.err.println("Got bad value for sample");
        printUsage();
        System.exit(ERROR_CODE);
    }
  }

  /**
   * Query the Shakespeare dataset for words with count at least {@code minWordCount} in the corpus
   * {@code corpus}.
   */
  // [START bigquery_query_params]
  private static void runNamed(final String corpus, final long minWordCount)
      throws InterruptedException {
    BigQuery bigquery =
        new BigQueryOptions.DefaultBigqueryFactory().create(BigQueryOptions.getDefaultInstance());

    String queryString = "SELECT word, word_count\n"
        + "FROM `bigquery-public-data.samples.shakespeare`\n"
        + "WHERE corpus = @corpus\n"
        + "AND word_count >= @min_word_count\n"
        + "ORDER BY word_count DESC";
    QueryRequest queryRequest =
        QueryRequest.newBuilder(queryString)
            .addNamedParameter("corpus", QueryParameterValue.string(corpus))
            .addNamedParameter("min_word_count", QueryParameterValue.int64(minWordCount))
            // Standard SQL syntax is required for parameterized queries.
            // See: https://cloud.google.com/bigquery/sql-reference/
            .setUseLegacySql(false)
            .build();

    // Execute the query.
    QueryResponse response = bigquery.query(queryRequest);

    // Wait for the job to finish (if the query takes more than 10 seconds to complete).
    while (!response.jobCompleted()) {
      Thread.sleep(1000);
      response = bigquery.getQueryResults(response.getJobId());
    }

    if (response.hasErrors()) {
      String firstError = "";
      if (response.getExecutionErrors().size() != 0) {
        firstError = response.getExecutionErrors().get(0).getMessage();
      }
      throw new RuntimeException(firstError);
    }

    QueryResult result = response.getResult();
    Iterator<List<FieldValue>> iter = result.iterateAll();

    while (iter.hasNext()) {
      List<FieldValue> row = iter.next();
      System.out.printf(
          "%s: %d\n",
          row.get(0).getStringValue(),
          row.get(1).getLongValue());
    }
  }
  // [END bigquery_query_params]

  /**
   * Query the baby names database to find the most popular names for a gender in a list of states.
   */
  // [START bigquery_query_params_arrays]
  private static void runArray(String gender, String[] states)
      throws InterruptedException {
    BigQuery bigquery =
        new BigQueryOptions.DefaultBigqueryFactory().create(BigQueryOptions.getDefaultInstance());

    String queryString = "SELECT name, sum(number) as count\n"
        + "FROM `bigquery-public-data.usa_names.usa_1910_2013`\n"
        + "WHERE gender = @gender\n"
        + "AND state IN UNNEST(@states)\n"
        + "GROUP BY name\n"
        + "ORDER BY count DESC\n"
        + "LIMIT 10;";
    QueryRequest queryRequest =
        QueryRequest.newBuilder(queryString)
            .addNamedParameter("gender", QueryParameterValue.string(gender))
            .addNamedParameter(
                "states",
                QueryParameterValue.array(states, String.class))
            // Standard SQL syntax is required for parameterized queries.
            // See: https://cloud.google.com/bigquery/sql-reference/
            .setUseLegacySql(false)
            .build();

    // Execute the query.
    QueryResponse response = bigquery.query(queryRequest);

    // Wait for the job to finish (if the query takes more than 10 seconds to complete).
    while (!response.jobCompleted()) {
      Thread.sleep(1000);
      response = bigquery.getQueryResults(response.getJobId());
    }

    if (response.hasErrors()) {
      String firstError = "";
      if (response.getExecutionErrors().size() != 0) {
        firstError = response.getExecutionErrors().get(0).getMessage();
      }
      throw new RuntimeException(firstError);
    }

    QueryResult result = response.getResult();
    Iterator<List<FieldValue>> iter = result.iterateAll();

    while (iter.hasNext()) {
      List<FieldValue> row = iter.next();
      System.out.printf("%s: %d\n", row.get(0).getStringValue(), row.get(1).getLongValue());
    }
  }
  // [END bigquery_query_params_arrays]

  // [START bigquery_query_params_timestamps]
  private static void runTimestamp() throws InterruptedException {
    BigQuery bigquery =
        new BigQueryOptions.DefaultBigqueryFactory().create(BigQueryOptions.getDefaultInstance());

    DateTime timestamp = new DateTime(2016, 12, 7, 8, 0, 0, DateTimeZone.UTC);

    String queryString = "SELECT TIMESTAMP_ADD(@ts_value, INTERVAL 1 HOUR);";
    QueryRequest queryRequest =
        QueryRequest.newBuilder(queryString)
            .addNamedParameter(
                "ts_value",
                QueryParameterValue.timestamp(
                    // Timestamp takes microseconds since 1970-01-01T00:00:00 UTC
                    timestamp.getMillis() * 1000))
            // Standard SQL syntax is required for parameterized queries.
            // See: https://cloud.google.com/bigquery/sql-reference/
            .setUseLegacySql(false)
            .build();

    // Execute the query.
    QueryResponse response = bigquery.query(queryRequest);

    // Wait for the job to finish (if the query takes more than 10 seconds to complete).
    while (!response.jobCompleted()) {
      Thread.sleep(1000);
      response = bigquery.getQueryResults(response.getJobId());
    }

    if (response.hasErrors()) {
      String firstError = "";
      if (response.getExecutionErrors().size() != 0) {
        firstError = response.getExecutionErrors().get(0).getMessage();
      }
      throw new RuntimeException(firstError);
    }

    QueryResult result = response.getResult();
    Iterator<List<FieldValue>> iter = result.iterateAll();

    DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();
    while (iter.hasNext()) {
      List<FieldValue> row = iter.next();
      System.out.printf(
          "%s\n",
          formatter.print(
              new DateTime(
                  // Timestamp values are returned in microseconds since 1970-01-01T00:00:00 UTC,
                  // but org.joda.time.DateTime constructor accepts times in milliseconds.
                  row.get(0).getTimestampValue() / 1000,
                  DateTimeZone.UTC)));
    }
  }
  // [END bigquery_query_params_timestamps]
}
