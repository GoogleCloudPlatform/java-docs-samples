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

// [START bigtable_reads_print]

import com.google.cloud.bigtable.hbase.BigtableConfiguration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.MultiRowRangeFilter;
import org.apache.hadoop.hbase.filter.MultiRowRangeFilter.RowRange;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.ValueFilter;
import org.apache.hadoop.hbase.util.Bytes;


public class Reads {

  // Write your code here.
  // [START_EXCLUDE]
  // [START bigtable_reads_row]
  public static void readRow() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String instanceId = "my-instance-id";
    String tableId = "mobile-time-series";
    readRow(projectId, instanceId, tableId);
  }

  public static void readRow(String projectId, String instanceId, String tableId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (Connection connection = BigtableConfiguration.connect(projectId, instanceId)) {
      Table table = connection.getTable(TableName.valueOf(tableId));

      byte[] rowkey = Bytes.toBytes("phone#4c410523#20190501");

      Result row = table.get(new Get(rowkey));
      printRow(row);

    } catch (IOException e) {
      System.out.println(
          "Unable to initialize service client, as a network error occurred: \n" + e.toString());
    }
  }
  // [END bigtable_reads_row]

  // [START bigtable_reads_row_partial]
  public static void readRowPartial() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String instanceId = "my-instance-id";
    String tableId = "mobile-time-series";
    readRowPartial(projectId, instanceId, tableId);
  }

  public static void readRowPartial(String projectId, String instanceId, String tableId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (Connection connection = BigtableConfiguration.connect(projectId, instanceId)) {
      Table table = connection.getTable(TableName.valueOf(tableId));
      byte[] rowkey = Bytes.toBytes("phone#4c410523#20190501");

      Result row =
          table.get(
              new Get(rowkey).addColumn(Bytes.toBytes("stats_summary"), Bytes.toBytes("os_build")));
      printRow(row);

    } catch (IOException e) {
      System.out.println(
          "Unable to initialize service client, as a network error occurred: \n" + e.toString());
    }
  }
  // [END bigtable_reads_row_partial]

  // [START bigtable_reads_rows]
  public static void readRows() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String instanceId = "my-instance-id";
    String tableId = "mobile-time-series";
    readRows(projectId, instanceId, tableId);
  }

  public static void readRows(String projectId, String instanceId, String tableId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (Connection connection = BigtableConfiguration.connect(projectId, instanceId)) {
      Table table = connection.getTable(TableName.valueOf(tableId));
      List<Get> queryRowList = new ArrayList<Get>();
      queryRowList.add(new Get(Bytes.toBytes("phone#4c410523#20190501")));
      queryRowList.add(new Get(Bytes.toBytes("phone#4c410523#20190502")));

      Result[] rows = table.get(queryRowList);

      for (Result row : rows) {
        printRow(row);
      }
    } catch (IOException e) {
      System.out.println(
          "Unable to initialize service client, as a network error occurred: \n" + e.toString());
    }
  }
  // [END bigtable_reads_rows]

  // [START bigtable_reads_row_range]
  public static void readRowRange() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String instanceId = "my-instance-id";
    String tableId = "mobile-time-series";
    readRowRange(projectId, instanceId, tableId);
  }

  public static void readRowRange(String projectId, String instanceId, String tableId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (Connection connection = BigtableConfiguration.connect(projectId, instanceId)) {
      Table table = connection.getTable(TableName.valueOf(tableId));

      Scan rangeQuery =
          new Scan()
              .withStartRow(Bytes.toBytes("phone#4c410523#20190501"))
              .withStopRow(Bytes.toBytes("phone#4c410523#201906201"));

      ResultScanner rows = table.getScanner(rangeQuery);

      for (Result row : rows) {
        printRow(row);
      }

    } catch (IOException e) {
      System.out.println(
          "Unable to initialize service client, as a network error occurred: \n" + e.toString());
    }
  }
  // [END bigtable_reads_row_range]

  // [START bigtable_reads_row_ranges]
  public static void readRowRanges() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String instanceId = "my-instance-id";
    String tableId = "mobile-time-series";
    readRowRanges(projectId, instanceId, tableId);
  }

  public static void readRowRanges(String projectId, String instanceId, String tableId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (Connection connection = BigtableConfiguration.connect(projectId, instanceId)) {
      Table table = connection.getTable(TableName.valueOf(tableId));
      List<RowRange> ranges = new ArrayList<>();

      ranges.add(
          new RowRange(
              Bytes.toBytes("phone#4c410523#20190501"),
              true,
              Bytes.toBytes("phone#4c410523#20190601"),
              false));
      ranges.add(
          new RowRange(
              Bytes.toBytes("phone#5c10102#20190501"),
              true,
              Bytes.toBytes("phone#5c10102#20190601"),
              false));
      Filter filter = new MultiRowRangeFilter(ranges);
      Scan scan = new Scan().setFilter(filter);

      ResultScanner rows = table.getScanner(scan);

      for (Result row : rows) {
        printRow(row);
      }
    } catch (IOException e) {
      System.out.println(
          "Unable to initialize service client, as a network error occurred: \n" + e.toString());
    }
  }
  // [END bigtable_reads_row_ranges]

  // [START bigtable_reads_prefix]
  public static void readPrefix() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String instanceId = "my-instance-id";
    String tableId = "mobile-time-series";
    readPrefix(projectId, instanceId, tableId);
  }

  public static void readPrefix(String projectId, String instanceId, String tableId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (Connection connection = BigtableConfiguration.connect(projectId, instanceId)) {
      Table table = connection.getTable(TableName.valueOf(tableId));
      Scan prefixScan = new Scan().setRowPrefixFilter(Bytes.toBytes("phone"));
      ResultScanner rows = table.getScanner(prefixScan);

      for (Result row : rows) {
        printRow(row);
      }
    } catch (IOException e) {
      System.out.println(
          "Unable to initialize service client, as a network error occurred: \n" + e.toString());
    }
  }
  // [END bigtable_reads_prefix]

  // [START bigtable_reads_filter]
  public static void readFilter() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String instanceId = "my-instance-id";
    String tableId = "mobile-time-series";
    readFilter(projectId, instanceId, tableId);
  }

  public static void readFilter(String projectId, String instanceId, String tableId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (Connection connection = BigtableConfiguration.connect(projectId, instanceId)) {
      Table table = connection.getTable(TableName.valueOf(tableId));

      ValueFilter valueFilter =
          new ValueFilter(CompareOp.EQUAL, new RegexStringComparator("PQ2A.*"));
      Scan scan = new Scan().setFilter(valueFilter);

      ResultScanner rows = table.getScanner(scan);

      for (Result row : rows) {
        printRow(row);
      }
    } catch (IOException e) {
      System.out.println(
          "Unable to initialize service client, as a network error occurred: \n" + e.toString());
    }
  }
  // [END bigtable_reads_filter]
  // [END_EXCLUDE]

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
// [END bigtable_reads_print]
