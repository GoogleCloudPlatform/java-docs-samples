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

package com.example.bigtable;

// [START bigtable_filters_print]

import static com.google.cloud.bigtable.data.v2.models.Filters.FILTERS;

import com.google.api.gax.rpc.ServerStream;
import com.google.bigtable.v2.ColumnRange;
import com.google.cloud.bigtable.hbase.BigtableConfiguration;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.ColumnCountGetFilter;
import org.apache.hadoop.hbase.filter.ColumnPaginationFilter;
import org.apache.hadoop.hbase.filter.ColumnRangeFilter;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FamilyFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.FilterList.Operator;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.apache.hadoop.hbase.filter.RandomRowFilter;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.SkipFilter;
import org.apache.hadoop.hbase.filter.ValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

public class Filters {

  // Write your code here.
  // [START_EXCLUDE]
  // [START bigtable_filters_limit_row_sample]
  public static void filterLimitRowSample() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String instanceId = "my-instance-id";
    String tableId = "mobile-time-series";
    filterLimitRowSample(projectId, instanceId, tableId);
  }

  public static void filterLimitRowSample(String projectId, String instanceId, String tableId) {
    // A filter that matches cells from a row with probability .75
    Filter filter = new RandomRowFilter(.75f);
    Scan scan = new Scan().setFilter(filter);
    readWithFilter(projectId, instanceId, tableId, scan);
  }
  // [END bigtable_filters_limit_row_sample]

  // [START bigtable_filters_limit_row_regex]
  public static void filterLimitRowRegex() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String instanceId = "my-instance-id";
    String tableId = "mobile-time-series";
    filterLimitRowRegex(projectId, instanceId, tableId);
  }

  public static void filterLimitRowRegex(String projectId, String instanceId, String tableId) {
    // A filter that matches cells from rows whose keys satisfy the given regex
    Filter filter = new RowFilter(CompareOp.EQUAL, new RegexStringComparator(".*#20190501$"));
    Scan scan = new Scan().setFilter(filter).setMaxVersions();
    readWithFilter(projectId, instanceId, tableId, scan);
  }
  // [END bigtable_filters_limit_row_regex]

  // [START bigtable_filters_limit_cells_per_col]
  public static void filterLimitCellsPerCol() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String instanceId = "my-instance-id";
    String tableId = "mobile-time-series";
    filterLimitCellsPerCol(projectId, instanceId, tableId);
  }

  public static void filterLimitCellsPerCol(String projectId, String instanceId, String tableId) {
    // A filter that matches only the most recent 2 cells within each column
    Scan scan = new Scan().setMaxVersions(2);
    readWithFilter(projectId, instanceId, tableId, scan);
  }
  // [END bigtable_filters_limit_cells_per_col]

  // [START bigtable_filters_limit_cells_per_row]
  public static void filterLimitCellsPerRow() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String instanceId = "my-instance-id";
    String tableId = "mobile-time-series";
    filterLimitCellsPerRow(projectId, instanceId, tableId);
  }

  public static void filterLimitCellsPerRow(String projectId, String instanceId, String tableId) {
    // A filter that matches the first 2 cells of each row
    //    Filter filter = new ColumnCountGetFilter(2);
    Filter filter = new ColumnPaginationFilter(2, 0);

    Scan scan = new Scan().setFilter(filter);
    readWithFilter(projectId, instanceId, tableId, scan);
  }
  // [END bigtable_filters_limit_cells_per_row]

  // [START bigtable_filters_limit_cells_per_row_offset]
  public static void filterLimitCellsPerRowOffset() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String instanceId = "my-instance-id";
    String tableId = "mobile-time-series";
    filterLimitCellsPerRowOffset(projectId, instanceId, tableId);
  }

  public static void filterLimitCellsPerRowOffset(
      String projectId, String instanceId, String tableId) {
    // A filter that skips the first 2 cells per row
    Filter filter = new ColumnPaginationFilter(Integer.MAX_VALUE, 2);
    Scan scan = new Scan().setFilter(filter);
    readWithFilter(projectId, instanceId, tableId, scan);
  }
  // [END bigtable_filters_limit_cells_per_row_offset]

  // [START bigtable_filters_limit_col_family_regex]
  public static void filterLimitColFamilyRegex() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String instanceId = "my-instance-id";
    String tableId = "mobile-time-series";
    filterLimitColFamilyRegex(projectId, instanceId, tableId);
  }

  public static void filterLimitColFamilyRegex(
      String projectId, String instanceId, String tableId) {
    // A filter that matches cells whose column family satisfies the given regex
    Filter filter = new FamilyFilter(CompareOp.EQUAL, new RegexStringComparator("stats_.*$"));
    Scan scan = new Scan().setFilter(filter);
    readWithFilter(projectId, instanceId, tableId, scan);
  }
  // [END bigtable_filters_limit_col_family_regex]

  // [START bigtable_filters_limit_col_qualifier_regex]
  public static void filterLimitColQualifierRegex() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String instanceId = "my-instance-id";
    String tableId = "mobile-time-series";
    filterLimitColQualifierRegex(projectId, instanceId, tableId);
  }

  public static void filterLimitColQualifierRegex(
      String projectId, String instanceId, String tableId) {
    // A filter that matches cells whose column qualifier satisfies the given regex
    Filter filter =
        new QualifierFilter(CompareOp.EQUAL, new RegexStringComparator("connected_.*$"));
    Scan scan = new Scan().setFilter(filter);
    readWithFilter(projectId, instanceId, tableId, scan);
  }
  // [END bigtable_filters_limit_col_qualifier_regex]

  // [START bigtable_filters_limit_col_range]
  public static void filterLimitColRange() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String instanceId = "my-instance-id";
    String tableId = "mobile-time-series";
    filterLimitColRange(projectId, instanceId, tableId);
  }

  public static void filterLimitColRange(String projectId, String instanceId, String tableId) {
    // A filter that matches cells whose column qualifiers are between data_plan_01gb and
    // data_plan_10gb in the column family cell_plan
    Filter filter =
        new ColumnRangeFilter(
            Bytes.toBytes("data_plan_01gb"), true, Bytes.toBytes("data_plan_10gb"), false);
    Scan scan = new Scan().addFamily(Bytes.toBytes("cell_plan")).setFilter(filter).setMaxVersions();
    readWithFilter(projectId, instanceId, tableId, scan);
  }
  // [END bigtable_filters_limit_col_range]

  // [START bigtable_filters_limit_value_range]
  public static void filterLimitValueRange() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String instanceId = "my-instance-id";
    String tableId = "mobile-time-series";
    filterLimitValueRange(projectId, instanceId, tableId);
  }

  public static void filterLimitValueRange(String projectId, String instanceId, String tableId) {
    // A filter that matches cells whose values are between the given values
    ValueFilter valueGreaterFilter =
        new ValueFilter(
            CompareFilter.CompareOp.GREATER_OR_EQUAL,
            new BinaryComparator(Bytes.toBytes("PQ2A.190405")));
    ValueFilter valueLesserFilter =
        new ValueFilter(
            CompareFilter.CompareOp.LESS_OR_EQUAL,
            new BinaryComparator(Bytes.toBytes("PQ2A.190406")));

    FilterList filter = new FilterList(FilterList.Operator.MUST_PASS_ALL);
    filter.addFilter(valueGreaterFilter);
    filter.addFilter(valueLesserFilter);

    Scan scan = new Scan().setFilter(filter);
    readWithFilter(projectId, instanceId, tableId, scan);
  }
  // [END bigtable_filters_limit_value_range]

  // [START bigtable_filters_limit_value_regex]
  public static void filterLimitValueRegex() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String instanceId = "my-instance-id";
    String tableId = "mobile-time-series";
    filterLimitValueRegex(projectId, instanceId, tableId);
  }

  public static void filterLimitValueRegex(String projectId, String instanceId, String tableId) {
    // A filter that matches cells whose value satisfies the given regex
    Filter filter = new ValueFilter(CompareOp.EQUAL, new RegexStringComparator("PQ2A.*$"));

    Scan scan = new Scan().setFilter(filter);
    readWithFilter(projectId, instanceId, tableId, scan);
  }
  // [END bigtable_filters_limit_value_regex]

  // [START bigtable_filters_limit_timestamp_range]
  public static void filterLimitTimestampRange() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String instanceId = "my-instance-id";
    String tableId = "mobile-time-series";
    filterLimitTimestampRange(projectId, instanceId, tableId);
  }

  public static void filterLimitTimestampRange(
      String projectId, String instanceId, String tableId) {
    // A filter that matches cells whose timestamp is from an hour ago or earlier
    // Get a time representing one hour ago
    long timestamp = Instant.now().minus(1, ChronoUnit.HOURS).toEpochMilli();
    try {
      Scan scan = new Scan().setTimeRange(0, timestamp).setMaxVersions();
      readWithFilter(projectId, instanceId, tableId, scan);
    } catch (IOException e) {
      System.out.println("There was an issue with your timestamp \n" + e.toString());
    }
  }
  // [END bigtable_filters_limit_timestamp_range]

  // [START bigtable_filters_limit_block_all]
  public static void filterLimitBlockAll() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String instanceId = "my-instance-id";
    String tableId = "mobile-time-series";
    filterLimitBlockAll(projectId, instanceId, tableId);
  }

  public static void filterLimitBlockAll(String projectId, String instanceId, String tableId) {
    // A filter that does not match any cells
    Filter filter = new SkipFilter(new RandomRowFilter(1));
    Scan scan = new Scan().setFilter(filter);
    readWithFilter(projectId, instanceId, tableId, scan);
  }
  // [END bigtable_filters_limit_block_all]

  // [START bigtable_filters_composing_chain]
  public static void filterComposingChain() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String instanceId = "my-instance-id";
    String tableId = "mobile-time-series";
    filterComposingChain(projectId, instanceId, tableId);
  }

  public static void filterComposingChain(String projectId, String instanceId, String tableId) {
    // A filter that selects one cell per row AND within the column family cell_plan
    Filter familyFilter =
        new FamilyFilter(CompareOp.EQUAL, new BinaryComparator(Bytes.toBytes("cell_plan")));
    Filter columnCountGetFilter = new ColumnCountGetFilter(3);

    FilterList filter = new FilterList(FilterList.Operator.MUST_PASS_ALL);
    filter.addFilter(columnCountGetFilter);
    filter.addFilter(familyFilter);
    Scan scan = new Scan().setFilter(filter);
    readWithFilter(projectId, instanceId, tableId, scan);
  }
  // [END bigtable_filters_composing_chain]

  // [START bigtable_filters_composing_interleave]
  public static void filterComposingInterleave() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String instanceId = "my-instance-id";
    String tableId = "mobile-time-series";
    filterComposingInterleave(projectId, instanceId, tableId);
  }

  public static void filterComposingInterleave(
      String projectId, String instanceId, String tableId) {
    // A filter that matches cells with the value true OR with the column qualifier os_build
    Filter qualifierFilter =
        new QualifierFilter(CompareOp.EQUAL, new BinaryComparator(Bytes.toBytes("os_build")));
    Filter valueFilter =
        new ValueFilter(CompareOp.EQUAL, new BinaryComparator(Bytes.toBytes("true")));

    FilterList filter = new FilterList(Operator.MUST_PASS_ONE);
    filter.addFilter(qualifierFilter);
    filter.addFilter(valueFilter);

    Scan scan = new Scan().setFilter(filter).setMaxVersions();
    readWithFilter(projectId, instanceId, tableId, scan);
  }
  // [END bigtable_filters_composing_interleave]
  // [END_EXCLUDE]

  public static void readWithFilter(
      String projectId, String instanceId, String tableId, Scan scan) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (Connection connection = BigtableConfiguration.connect(projectId, instanceId)) {
      Table table = connection.getTable(TableName.valueOf(tableId));

      ResultScanner rows = table.getScanner(scan);

      for (Result row : rows) {
        printRow(row);
      }
    } catch (IOException e) {
      System.out.println(
          "Unable to initialize service client, as a network error occurred: \n" + e.toString());
    }
  }

  private static void printRow(Result row) {
    System.out.printf("Reading data for %s%n", Bytes.toString(row.rawCells()[0].getRowArray()));
    String colFamily = "";
    for (Cell cell : row.rawCells()) {
      String currentFamily = Bytes.toString(cell.getFamilyArray());
      if (!currentFamily.equals(colFamily)) {
        colFamily = currentFamily;
        System.out.printf("Column Family %s%n", colFamily);
      }
      System.out.printf(
          "\t%s: %s @%s%n",
          Bytes.toString(cell.getQualifierArray()),
          Bytes.toString(cell.getValueArray()),
          cell.getTimestamp());
    }
    System.out.println();
  }
}
// [END bigtable_filters_print]
